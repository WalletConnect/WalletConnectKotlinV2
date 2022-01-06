package com.walletconnect.walletconnectv2.relay.walletconnect

import com.tinder.scarlet.WebSocket
import com.walletconnect.walletconnectv2.clientsync.ClientSyncJsonRpc
import com.walletconnect.walletconnectv2.common.SubscriptionId
import com.walletconnect.walletconnectv2.common.Topic
import com.walletconnect.walletconnectv2.errors.exception
import com.walletconnect.walletconnectv2.jsonrpc.JsonRpcSerializer
import com.walletconnect.walletconnectv2.jsonrpc.history.JsonRpcHistory
import com.walletconnect.walletconnectv2.jsonrpc.model.ClientJsonRpc
import com.walletconnect.walletconnectv2.jsonrpc.model.JsonRpcResponse
import com.walletconnect.walletconnectv2.jsonrpc.model.WCRequestSubscriptionPayload
import com.walletconnect.walletconnectv2.relay.waku.Relay
import com.walletconnect.walletconnectv2.relay.waku.WakuNetworkRepository
import com.walletconnect.walletconnectv2.scope
import com.walletconnect.walletconnectv2.util.Logger
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import javax.inject.Inject

class WalletConnectRelayer @Inject constructor(
    private val networkRepository: WakuNetworkRepository,
    private val serializer: JsonRpcSerializer,
    private val jsonRpcHistory: JsonRpcHistory
) {
    val isConnectionOpened = MutableStateFlow(false)

    private val _clientSyncJsonRpc: MutableSharedFlow<WCRequestSubscriptionPayload> = MutableSharedFlow()
    val clientSyncJsonRpc: SharedFlow<WCRequestSubscriptionPayload> = _clientSyncJsonRpc

    private val peerResponse: MutableSharedFlow<JsonRpcResponse> = MutableSharedFlow()
    private val subscriptions: MutableMap<String, String> = mutableMapOf()
    private val exceptionHandler = CoroutineExceptionHandler { _, exception -> Logger.error(exception) }

    init {
        handleInitialisationErrors()
        manageSubscriptions()
    }

    fun request(topic: Topic, payload: ClientSyncJsonRpc, onResult: (Result<JsonRpcResponse.JsonRpcResult>) -> Unit) {
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

            networkRepository.publish(topic, serializer.serialize(payload, topic)) { result ->
                result.fold(
                    onSuccess = {},
                    onFailure = { error -> onResult(Result.failure(error)) }
                )
            }
        }
    }

    fun respond(topic: Topic, response: JsonRpcResponse, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
        networkRepository.publish(topic, serializer.serialize(response, topic)) { result ->
            result.fold(
                onSuccess = { onSuccess() },
                onFailure = { error -> onFailure(error) }
            )
        }
    }

    fun subscribe(topic: Topic) {
        networkRepository.subscribe(topic) { result ->
            result.fold(
                onSuccess = { acknowledgement -> subscriptions[topic.value] = acknowledgement.result.id },
                onFailure = { error -> Logger.error("Subscribe to topic: $topic error: $error") }
            )
        }
    }

    fun unsubscribe(topic: Topic) {
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
}