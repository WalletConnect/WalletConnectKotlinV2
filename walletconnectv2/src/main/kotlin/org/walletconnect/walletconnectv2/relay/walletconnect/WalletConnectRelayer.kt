package org.walletconnect.walletconnectv2.relay.walletconnect

import android.app.Application
import com.tinder.scarlet.WebSocket
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import org.walletconnect.walletconnectv2.clientsync.ClientSyncJsonRpc
import org.walletconnect.walletconnectv2.common.SubscriptionId
import org.walletconnect.walletconnectv2.common.Topic
import org.walletconnect.walletconnectv2.common.toWakuNetworkInitParams
import org.walletconnect.walletconnectv2.errors.exception
import org.walletconnect.walletconnectv2.jsonrpc.JsonRpcSerializer
import org.walletconnect.walletconnectv2.jsonrpc.history.JsonRpcHistory
import org.walletconnect.walletconnectv2.jsonrpc.model.ClientJsonRpc
import org.walletconnect.walletconnectv2.jsonrpc.model.JsonRpcResponse
import org.walletconnect.walletconnectv2.jsonrpc.model.WCRequestSubscriptionPayload
import org.walletconnect.walletconnectv2.relay.waku.Relay
import org.walletconnect.walletconnectv2.relay.waku.WakuNetworkRepository
import org.walletconnect.walletconnectv2.scope
import org.walletconnect.walletconnectv2.util.Logger

class WalletConnectRelayer {
    //Region: Move to DI
    private lateinit var networkRepository: WakuNetworkRepository
    private val serializer: JsonRpcSerializer = JsonRpcSerializer()
    //end

    private val _clientSyncJsonRpc: MutableSharedFlow<WCRequestSubscriptionPayload> = MutableSharedFlow()
    val clientSyncJsonRpc: SharedFlow<WCRequestSubscriptionPayload> = _clientSyncJsonRpc

    private val peerResponse: MutableSharedFlow<JsonRpcResponse> = MutableSharedFlow()

    private val subscriptions: MutableMap<String, String> = mutableMapOf()
    private val jsonRpcHistory: JsonRpcHistory = JsonRpcHistory()
    val isConnectionOpened = MutableStateFlow(false)

    internal fun initialize(relay: RelayFactory) {
        networkRepository = WakuNetworkRepository.init(relay.toWakuNetworkInitParams())
        handleInitialisationErrors()
        manageSubscriptions()
    }

    fun request(topic: Topic, payload: ClientSyncJsonRpc, onResult: (Result<JsonRpcResponse.JsonRpcResult>) -> Unit) {
        require(::networkRepository.isInitialized)

        if (jsonRpcHistory.setRequest(payload.id, topic)) {
            scope.launch {
                supervisorScope {
                    peerResponse
                        .filter { response -> response.id == payload.id }
                        .collect { response ->
                            when (response) {
                                is JsonRpcResponse.JsonRpcResult -> onResult(Result.success(response))
                                is JsonRpcResponse.JsonRpcError -> onResult(Result.failure(Throwable(response.error.message)))
                            }
                            cancel()
                        }
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

    fun respond(topic: Topic, response: JsonRpcResponse, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
        require(::networkRepository.isInitialized)

        networkRepository.publish(topic, serializer.serialize(response, topic)) { result ->
            result.fold(
                onSuccess = { onSuccess() },
                onFailure = { error -> onFailure(error) }
            )
        }
    }

    fun subscribe(topic: Topic) {
        require(::networkRepository.isInitialized)

        networkRepository.subscribe(topic) { result ->
            result.fold(
                onSuccess = { acknowledgement -> subscriptions[topic.value] = acknowledgement.result.id },
                onFailure = { error -> Logger.error("Subscribe to topic: $topic error: $error") }
            )
        }
    }

    fun unsubscribe(topic: Topic) {
        require(::networkRepository.isInitialized)

        if (subscriptions.contains(topic.value)) {
            val subscriptionId = SubscriptionId(subscriptions[topic.value].toString())
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

    private suspend fun handleSessionRequest(decryptedMessage: String, topic: Topic) {
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
            peerResponse.emit(JsonRpcResponse.JsonRpcResult(acknowledgement.id, acknowledgement.result.toString()))
        }

        val error = serializer.tryDeserialize<Relay.Subscription.JsonRpcError>(decryptedMessage)
        if (error != null) {
            peerResponse.emit(JsonRpcResponse.JsonRpcError(error.id, JsonRpcResponse.Error(error.error.code, error.error.message)))
        }
    }

    private val exceptionHandler = CoroutineExceptionHandler { _, exception -> Logger.error(exception) }

    class RelayFactory(val useTls: Boolean, val hostName: String, val apiKey: String, val application: Application)
}