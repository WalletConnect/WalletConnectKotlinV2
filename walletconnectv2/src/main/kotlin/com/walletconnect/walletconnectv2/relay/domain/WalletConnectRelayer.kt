package com.walletconnect.walletconnectv2.relay.domain

import android.app.Application
import com.tinder.scarlet.WebSocket
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import com.walletconnect.walletconnectv2.relay.model.clientsync.ClientSyncJsonRpc
import com.walletconnect.walletconnectv2.common.model.vo.SubscriptionIdVO
import com.walletconnect.walletconnectv2.common.model.vo.TopicVO
import com.walletconnect.walletconnectv2.common.errors.exception
import com.walletconnect.walletconnectv2.common.model.vo.JsonRpcResponseVO
import com.walletconnect.walletconnectv2.network.model.Relay
import com.walletconnect.walletconnectv2.network.data.WakuNetworkRepository
import com.walletconnect.walletconnectv2.common.scope
import com.walletconnect.walletconnectv2.relay.data.JsonRpcSerializer
import com.walletconnect.walletconnectv2.relay.model.ClientJsonRpc
import com.walletconnect.walletconnectv2.relay.model.WCRequestSubscriptionPayload
import com.walletconnect.walletconnectv2.storage.history.JsonRpcHistory
import com.walletconnect.walletconnectv2.util.Logger

class WalletConnectRelayer {
    //Region: Move to DI
    private lateinit var networkRepository: WakuNetworkRepository
    private val serializer: JsonRpcSerializer = JsonRpcSerializer()
    //end

    private val _clientSyncJsonRpc: MutableSharedFlow<WCRequestSubscriptionPayload> = MutableSharedFlow()
    internal val clientSyncJsonRpc: SharedFlow<WCRequestSubscriptionPayload> = _clientSyncJsonRpc

    private val peerResponse: MutableSharedFlow<JsonRpcResponseVO> = MutableSharedFlow()

    private val subscriptions: MutableMap<String, String> = mutableMapOf()
    private val jsonRpcHistory: JsonRpcHistory = JsonRpcHistory()
    val isConnectionOpened = MutableStateFlow(false)

    internal fun initialize(relay: RelayFactory) {
        networkRepository = WakuNetworkRepository.init(relay.toWakuNetworkInitParams())
        handleInitialisationErrors()
        manageSubscriptions()
    }

    internal fun request(topic: TopicVO, payload: ClientSyncJsonRpc, onResult: (Result<JsonRpcResponseVO.JsonRpcResult>) -> Unit) {
        require(::networkRepository.isInitialized)

        if (jsonRpcHistory.setRequest(payload.id, topic)) {
            scope.launch {
                supervisorScope {
                    peerResponse
                        .filter { response -> response.id == payload.id }
                        .collect { response ->
                            when (response) {
                                is JsonRpcResponseVO.JsonRpcResult -> onResult(Result.success(response))
                                is JsonRpcResponseVO.JsonRpcError -> onResult(Result.failure(Throwable(response.error.message)))
                            }
                            cancel()
                        }
                }
            }

            networkRepository.publish(topic, serializer.serialize(payload, topic)) { result ->
                result.fold(
                    onSuccess = {},
                    onFailure = { error -> onResult(Result.failure(error)) }
                )
            }
        }
    }

    fun respond(topic: TopicVO, response: JsonRpcResponseVO, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
        require(::networkRepository.isInitialized)

        networkRepository.publish(topic, serializer.serialize(response, topic)) { result ->
            result.fold(
                onSuccess = { onSuccess() },
                onFailure = { error -> onFailure(error) }
            )
        }
    }

    fun subscribe(topic: TopicVO) {
        require(::networkRepository.isInitialized)

        networkRepository.subscribe(topic) { result ->
            result.fold(
                onSuccess = { acknowledgement -> subscriptions[topic.value] = acknowledgement.result.id },
                onFailure = { error -> Logger.error("Subscribe to topic: $topic error: $error") }
            )
        }
    }

    fun unsubscribe(topic: TopicVO) {
        require(::networkRepository.isInitialized)

        if (subscriptions.contains(topic.value)) {
            val subscriptionId = SubscriptionIdVO(subscriptions[topic.value].toString())
            networkRepository.unsubscribe(topic, subscriptionId) { result ->
                result.fold(
                    onSuccess = {
                        jsonRpcHistory.deleteRequests(topic)
                        subscriptions.remove(topic.value)
                    },
                    onFailure = { error -> Logger.error("Unsubscribe to topic: $topic error: $error") }
                )
            }
        }
    }

    private fun handleInitialisationErrors() {
        scope.launch(exceptionHandler) {
            networkRepository.eventsFlow
                .onEach { event -> Logger.log("$event") }
                .onEach { event: WebSocket.Event ->
                    if (event is WebSocket.Event.OnConnectionOpened<*>) {
                        isConnectionOpened.compareAndSet(expect = false, update = true)
                    } else if (event is WebSocket.Event.OnConnectionClosed) {
                        isConnectionOpened.compareAndSet(expect = true, update = false)
                    }
                }
                .filterIsInstance<WebSocket.Event.OnConnectionFailed>()
                .collect { event ->
                    Logger.error(event.throwable.stackTraceToString())
                    throw event.throwable.exception
                }
        }
    }

    private fun manageSubscriptions() {
        scope.launch(exceptionHandler) {
            networkRepository.subscriptionRequest
                .map { relayRequest ->
                    val decodedMessage = serializer.decode(relayRequest.message, relayRequest.subscriptionTopic)
                    val topic = relayRequest.subscriptionTopic
                    Pair(decodedMessage, topic)
                }
                .collect { (decryptedMessage, topic) ->
                    handleSessionRequest(decryptedMessage, topic)
                    handleJsonRpcResponse(decryptedMessage)
                }
        }
    }

    private suspend fun handleSessionRequest(decryptedMessage: String, topic: TopicVO) {
        val clientJsonRpc = serializer.tryDeserialize<ClientJsonRpc>(decryptedMessage)

        if (clientJsonRpc != null && jsonRpcHistory.setRequest(clientJsonRpc.id, topic)) {
            serializer.deserialize(clientJsonRpc.method, decryptedMessage)?.let { params ->
                _clientSyncJsonRpc.emit(WCRequestSubscriptionPayload(clientJsonRpc.id, topic, clientJsonRpc.method, params))
            }
        }
    }

    private suspend fun handleJsonRpcResponse(decryptedMessage: String) {
        val acknowledgement = serializer.tryDeserialize<Relay.Subscription.Acknowledgement>(decryptedMessage)
        if (acknowledgement != null) {
            peerResponse.emit(JsonRpcResponseVO.JsonRpcResult(acknowledgement.id, acknowledgement.result.toString()))
        }

        val error = serializer.tryDeserialize<Relay.Subscription.JsonRpcError>(decryptedMessage)
        if (error != null) {
            peerResponse.emit(JsonRpcResponseVO.JsonRpcError(error.id, JsonRpcResponseVO.Error(error.error.code, error.error.message)))
        }
    }

    private val exceptionHandler = CoroutineExceptionHandler { _, exception -> Logger.error(exception) }

    private fun RelayFactory.toWakuNetworkInitParams(): WakuNetworkRepository.WakuNetworkFactory =
        WakuNetworkRepository.WakuNetworkFactory(useTls, hostName, projectId, application)

    class RelayFactory(val useTls: Boolean, val hostName: String, val projectId: String, val application: Application)
}