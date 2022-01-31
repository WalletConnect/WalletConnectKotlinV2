package com.walletconnect.walletconnectv2.relay.domain

import com.tinder.scarlet.WebSocket
import com.walletconnect.walletconnectv2.core.exceptions.WalletConnectException
import com.walletconnect.walletconnectv2.core.model.type.ClientSyncJsonRpc
import com.walletconnect.walletconnectv2.core.model.vo.JsonRpcResponseVO
import com.walletconnect.walletconnectv2.core.model.vo.RequestSubscriptionPayloadVO
import com.walletconnect.walletconnectv2.core.model.vo.SubscriptionIdVO
import com.walletconnect.walletconnectv2.core.model.vo.TopicVO
import com.walletconnect.walletconnectv2.core.scope.scope
import com.walletconnect.walletconnectv2.network.NetworkRepository
import com.walletconnect.walletconnectv2.relay.data.serializer.JsonRpcSerializer
import com.walletconnect.walletconnectv2.relay.model.RelayDO
import com.walletconnect.walletconnectv2.relay.model.mapper.toJsonRpcResultVO
import com.walletconnect.walletconnectv2.relay.model.mapper.toRelayDOJsonRpcResponse
import com.walletconnect.walletconnectv2.storage.history.JsonRpcHistory
import com.walletconnect.walletconnectv2.util.Logger
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import java.net.HttpURLConnection

internal class WalletConnectRelayer(
    private val networkRepository: NetworkRepository,
    private val serializer: JsonRpcSerializer,
    private val jsonRpcHistory: JsonRpcHistory
) {
    private val _clientSyncJsonRpc: MutableSharedFlow<RequestSubscriptionPayloadVO> = MutableSharedFlow()
    internal val clientSyncJsonRpc: SharedFlow<RequestSubscriptionPayloadVO> = _clientSyncJsonRpc

    private val peerResponse: MutableSharedFlow<RelayDO.JsonRpcResponse> = MutableSharedFlow()
    private val subscriptions: MutableMap<String, String> = mutableMapOf()

    private val exceptionHandler = CoroutineExceptionHandler { _, exception -> Logger.error(exception) }
    val isConnectionOpened = MutableStateFlow(false)

    val initializationErrorsFlow: Flow<WalletConnectException>
        get() = networkRepository.eventsFlow
            .onEach { event -> Logger.log("$event") }
            .onEach { event: WebSocket.Event -> setOnConnectionOpen(event) }
            .filterIsInstance<WebSocket.Event.OnConnectionFailed>()
            .map { error -> error.throwable.toWalletConnectException }

    init {
        manageSubscriptions()
    }

    internal fun request(topic: TopicVO, payload: ClientSyncJsonRpc, onResult: (Result<JsonRpcResponseVO.JsonRpcResult>) -> Unit) {
        if (jsonRpcHistory.setRequest(payload.id, topic)) {
            scope.launch {
                supervisorScope {
                    peerResponse
                        .filter { response -> response.id == payload.id }
                        .collect { response ->
                            when (response) {
                                is RelayDO.JsonRpcResponse.JsonRpcResult -> onResult(Result.success(response.toJsonRpcResultVO()))
                                is RelayDO.JsonRpcResponse.JsonRpcError -> onResult(Result.failure(Throwable(response.error.message)))
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

    internal fun respond(topic: TopicVO, response: JsonRpcResponseVO, onSuccess: () -> Unit = {}, onFailure: (Throwable) -> Unit = {}) {
        networkRepository.publish(topic, serializer.serialize(response.toRelayDOJsonRpcResponse(), topic)) { result ->
            result.fold(
                onSuccess = { onSuccess() },
                onFailure = { error -> onFailure(error) }
            )
        }
    }

    internal fun subscribe(topic: TopicVO) {
        networkRepository.subscribe(topic) { result ->
            result.fold(
                onSuccess = { acknowledgement -> subscriptions[topic.value] = acknowledgement.result.id },
                onFailure = { error -> Logger.error("Subscribe to topic: $topic error: $error") }
            )
        }
    }

    internal fun unsubscribe(topic: TopicVO) {
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
        val clientJsonRpc = serializer.tryDeserialize<RelayDO.ClientJsonRpc>(decryptedMessage)
        if (clientJsonRpc != null && jsonRpcHistory.setRequest(clientJsonRpc.id, topic)) {
            serializer.deserialize(clientJsonRpc.method, decryptedMessage)?.let { params ->
                _clientSyncJsonRpc.emit(RequestSubscriptionPayloadVO(clientJsonRpc.id, topic, clientJsonRpc.method, params))
            }
        }
    }

    private suspend fun handleJsonRpcResponse(decryptedMessage: String) {
        val acknowledgement = serializer.tryDeserialize<RelayDO.JsonRpcResponse.JsonRpcResult>(decryptedMessage)
        if (acknowledgement != null) {
            peerResponse.emit(acknowledgement)
        }

        val error = serializer.tryDeserialize<RelayDO.JsonRpcResponse.JsonRpcError>(decryptedMessage)
        if (error != null) {
            peerResponse.emit(error)
        }
    }

    private fun setOnConnectionOpen(event: WebSocket.Event) {
        if (event is WebSocket.Event.OnConnectionOpened<*>) {
            isConnectionOpened.compareAndSet(expect = false, update = true)
        } else if (event is WebSocket.Event.OnConnectionClosed) {
            isConnectionOpened.compareAndSet(expect = true, update = false)
        }
    }

    @get:JvmSynthetic
    private val Throwable.toWalletConnectException: WalletConnectException
        get() =
            when {
                this.message?.contains(HttpURLConnection.HTTP_UNAUTHORIZED.toString()) == true ->
                    WalletConnectException.ProjectIdDoesNotExistException(this.message)
                this.message?.contains(HttpURLConnection.HTTP_FORBIDDEN.toString()) == true ->
                    WalletConnectException.InvalidProjectIdException(this.message)
                else -> WalletConnectException.ServerException(this.message)
            }
}