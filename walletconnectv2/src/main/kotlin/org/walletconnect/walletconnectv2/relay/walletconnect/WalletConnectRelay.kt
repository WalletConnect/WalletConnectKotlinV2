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
import org.walletconnect.walletconnectv2.jsonrpc.model.ClientJsonRpc
import org.walletconnect.walletconnectv2.jsonrpc.model.JsonRpcResponse
import org.walletconnect.walletconnectv2.jsonrpc.model.WCRequestSubscriptionPayload
import org.walletconnect.walletconnectv2.jsonrpc.utils.ClientJsonRpcSerializer
import org.walletconnect.walletconnectv2.relay.waku.Relay
import org.walletconnect.walletconnectv2.relay.waku.WakuNetworkRepository
import org.walletconnect.walletconnectv2.scope
import org.walletconnect.walletconnectv2.serailising.tryDeserialize
import org.walletconnect.walletconnectv2.util.Logger

class WalletConnectRelay {
    //Region: Move to DI
    private lateinit var networkRepository: WakuNetworkRepository
    private val serializer: JsonRpcSerializer = JsonRpcSerializer()
    //end

    private val _clientSyncJsonRpc: MutableSharedFlow<WCRequestSubscriptionPayload> = MutableSharedFlow()
    val clientSyncJsonRpc: SharedFlow<WCRequestSubscriptionPayload> = _clientSyncJsonRpc

    private val _peerResponse: MutableSharedFlow<JsonRpcResponse> = MutableSharedFlow()
    private val peerResponse: SharedFlow<JsonRpcResponse> = _peerResponse

    var isConnectionOpened = MutableStateFlow(false)

    internal fun initialize(relay: RelayFactory) {
        networkRepository = WakuNetworkRepository.init(relay.toWakuNetworkInitParams())
        handleInitialisationErrors()
        manageSubscriptions()
    }

    fun request(topic: Topic, payload: ClientSyncJsonRpc, onResult: (Result<JsonRpcResponse.JsonRpcResult>) -> Unit) {
        require(::networkRepository.isInitialized)
        scope.launch {
            supervisorScope {
                peerResponse
                    .filter { response -> response.id == payload.id }
                    .collect { response ->
                        when (response) {
                            is JsonRpcResponse.JsonRpcResult -> onResult(Result.success(response))
                            is JsonRpcResponse.JsonRpcError -> onResult(Result.failure(Throwable(response.error.message)))
                        }
                    }
                cancel()
            }
        }
        val encryptedMessage: String = serializer.serialize(payload, topic)
        networkRepository.publish(topic, encryptedMessage) { result ->
            result.fold(
                onSuccess = {},
                onFailure = { error -> onResult(Result.failure(error)) }
            )
        }
    }


    fun respond(topic: Topic, response: JsonRpcResponse, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
        require(::networkRepository.isInitialized)
        val encryptedMessage: String = serializer.serialize(response, topic)
        networkRepository.publish(topic, encryptedMessage) { result ->
            result.fold(
                onSuccess = { onSuccess() },
                onFailure = { error -> onFailure(error) }
            )
        }
    }

    fun subscribe(topic: Topic) {
        require(::networkRepository.isInitialized)
        networkRepository.subscribe(topic) { error ->
            Logger.error("Subscribe to topic: $topic error: $error")
        }
    }

    fun unsubscribe(topic: Topic) {
        require(::networkRepository.isInitialized)
        //TODO Add subscriptionId from local storage, based on topic
        networkRepository.unsubscribe(topic, SubscriptionId("1")) { error ->
            Logger.error("Unsubscribe to topic: $topic error: $error")
        }
    }

    private fun handleInitialisationErrors() {
        scope.launch(exceptionHandler) {
            networkRepository.eventsFlow
                .onEach { event -> Logger.log("$event") }
                .onEach { event: WebSocket.Event ->
                    if (event is WebSocket.Event.OnConnectionOpened<*>) {
                        isConnectionOpened.compareAndSet(expect = false, update = true)
                    }
                }
                .filterIsInstance<WebSocket.Event.OnConnectionFailed>()
                .collect { event -> throw event.throwable.exception }
        }
    }

    private fun manageSubscriptions() {
        scope.launch(exceptionHandler) {
            networkRepository.subscriptionRequest
                .map { peerRequest ->
                    val decodedMessage = serializer.decode(peerRequest.message, peerRequest.subscriptionTopic)
                    val topic = peerRequest.subscriptionTopic
                    Pair(decodedMessage, topic)
                }
                .collect { (decryptedMessage, topic) ->
                    handleSessionRequest(decryptedMessage, topic)
                    handleJsonRpcResponse(decryptedMessage)
                }
        }
    }

    private suspend fun handleSessionRequest(decryptedMessage: String, topic: Topic) {
        val clientJsonRpc = tryDeserialize<ClientJsonRpc>(decryptedMessage)
        if (clientJsonRpc != null) {
            ClientJsonRpcSerializer.deserialize(clientJsonRpc.method, decryptedMessage)?.let { params ->
                _clientSyncJsonRpc.emit(WCRequestSubscriptionPayload(clientJsonRpc.id, topic, clientJsonRpc.method, params))
            }
        }
    }

    private suspend fun handleJsonRpcResponse(decryptedMessage: String) {
        val acknowledgement = tryDeserialize<Relay.Subscription.Acknowledgement>(decryptedMessage)
        if (acknowledgement != null) {
            _peerResponse.emit(JsonRpcResponse.JsonRpcResult(acknowledgement.id, acknowledgement.result.toString()))
        }

        val error = tryDeserialize<Relay.Subscription.JsonRpcError>(decryptedMessage)
        if (error != null) {
            _peerResponse.emit(JsonRpcResponse.JsonRpcError(error.id, JsonRpcResponse.Error(error.error.code, error.error.message)))
        }
    }

    private val exceptionHandler = CoroutineExceptionHandler { _, exception -> Logger.error(exception) }

    class RelayFactory(val useTls: Boolean, val hostName: String, val apiKey: String, val application: Application)
}