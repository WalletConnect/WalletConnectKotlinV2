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
import org.walletconnect.walletconnectv2.jsonrpc.JsonRpcConverter
import org.walletconnect.walletconnectv2.jsonrpc.model.ClientJsonRpc
import org.walletconnect.walletconnectv2.jsonrpc.model.JsonRpcResponse
import org.walletconnect.walletconnectv2.jsonrpc.model.WCRequestSubscriptionPayload
import org.walletconnect.walletconnectv2.jsonrpc.utils.ClientJsonRpcSerializer
import org.walletconnect.walletconnectv2.relay.waku.Relay
import org.walletconnect.walletconnectv2.relay.waku.WakuNetworkRepository
import org.walletconnect.walletconnectv2.scope
import org.walletconnect.walletconnectv2.serailising.tryDeserialize
import org.walletconnect.walletconnectv2.serailising.trySerialize
import org.walletconnect.walletconnectv2.util.Logger

class WalletConnectRelay {

    //Region: Move to DI
    private lateinit var networkRepository: WakuNetworkRepository
    private val converter: JsonRpcConverter = JsonRpcConverter()
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
        val json = ClientJsonRpcSerializer.serialize(payload)
        val encryptedMessage = converter.encode(json, topic)

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

        networkRepository.publish(topic, encryptedMessage) { result ->
            result.fold(
                onSuccess = {},
                onFailure = { error -> onResult(Result.failure(error)) }
            )
        }
    }


    fun respond(topic: Topic, response: JsonRpcResponse, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
        require(::networkRepository.isInitialized)
        val json = trySerialize(response)
        val encryptedMessage = converter.encode(json, topic)

        networkRepository.publish(topic, encryptedMessage) { result ->
            result.fold(
                onSuccess = { onSuccess() },
                onFailure = { error -> onFailure(error) }
            )
        }
    }


    //todo relay error, return error to enging
    fun subscribe(topic: Topic) {
        require(::networkRepository.isInitialized)
        networkRepository.subscribe(topic)
    }

    //TODO get subscription based on the topic
    //todo relay error, return error to enging
    fun unsubscribe(topic: Topic) {
        require(::networkRepository.isInitialized)
        //TODO Add subscriptionId from local storage + Delete all data from local storage coupled with given session

        networkRepository.unsubscribe(topic, SubscriptionId("1"))
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
                    val decryptedMessage = converter.decode(peerRequest.message, peerRequest.subscriptionTopic)
                    val topic = peerRequest.subscriptionTopic
                    Pair(decryptedMessage, topic)
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