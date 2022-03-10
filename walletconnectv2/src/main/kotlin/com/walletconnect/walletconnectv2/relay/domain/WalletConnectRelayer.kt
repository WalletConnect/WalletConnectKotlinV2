package com.walletconnect.walletconnectv2.relay.domain

import com.tinder.scarlet.WebSocket
import com.walletconnect.walletconnectv2.core.exceptions.client.WalletConnectException
import com.walletconnect.walletconnectv2.core.exceptions.peer.PeerError
import com.walletconnect.walletconnectv2.core.model.type.SettlementSequence
import com.walletconnect.walletconnectv2.core.model.utils.JsonRpcMethod
import com.walletconnect.walletconnectv2.core.model.vo.SubscriptionIdVO
import com.walletconnect.walletconnectv2.core.model.vo.TopicVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.after.PostSettlementSessionVO
import com.walletconnect.walletconnectv2.core.model.vo.jsonRpc.JsonRpcResponseVO
import com.walletconnect.walletconnectv2.core.model.vo.sync.PendingRequestVO
import com.walletconnect.walletconnectv2.core.model.vo.sync.WCRequestVO
import com.walletconnect.walletconnectv2.core.model.vo.sync.WCResponseVO
import com.walletconnect.walletconnectv2.core.scope.scope
import com.walletconnect.walletconnectv2.network.NetworkRepository
import com.walletconnect.walletconnectv2.relay.data.serializer.JsonRpcSerializer
import com.walletconnect.walletconnectv2.relay.model.RelayDO
import com.walletconnect.walletconnectv2.relay.model.mapper.*
import com.walletconnect.walletconnectv2.storage.history.JsonRpcHistory
import com.walletconnect.walletconnectv2.util.Logger
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.net.HttpURLConnection

internal class WalletConnectRelayer(
    private val networkRepository: NetworkRepository,
    private val serializer: JsonRpcSerializer,
    private val jsonRpcHistory: JsonRpcHistory
) {
    private val _clientSyncJsonRpc: MutableSharedFlow<WCRequestVO> = MutableSharedFlow()
    internal val clientSyncJsonRpc: SharedFlow<WCRequestVO> = _clientSyncJsonRpc

    private val _peerResponse: MutableSharedFlow<WCResponseVO> = MutableSharedFlow()
    val peerResponse: SharedFlow<WCResponseVO> = _peerResponse

    private val _isConnectionOpened = MutableStateFlow(false)
    val isConnectionOpened: StateFlow<Boolean> = _isConnectionOpened

    private val subscriptions: MutableMap<String, String> = mutableMapOf()
    private val exceptionHandler = CoroutineExceptionHandler { _, exception -> Logger.error(exception) }

    @get:JvmSynthetic
    private val Throwable.toWalletConnectException: WalletConnectException
        get() =
            when {
                this.message?.contains(HttpURLConnection.HTTP_UNAUTHORIZED.toString()) == true ->
                    WalletConnectException.ProjectIdDoesNotExistException(this.message)
                this.message?.contains(HttpURLConnection.HTTP_FORBIDDEN.toString()) == true ->
                    WalletConnectException.InvalidProjectIdException(this.message)
                else -> WalletConnectException.GenericException(this.message)
            }

    val initializationErrorsFlow: Flow<WalletConnectException>
        get() = networkRepository.eventsFlow
            .onEach { event: WebSocket.Event ->
                Logger.log("$event")
                setOnConnectionOpen(event)
            }
            .filterIsInstance<WebSocket.Event.OnConnectionFailed>()
            .map { error -> error.throwable.toWalletConnectException }

    init {
        manageSubscriptions()
    }

    internal fun publishJsonRpcRequests(
        topic: TopicVO,
        payload: SettlementSequence<*>,
        prompt: Boolean = false,
        onSuccess: () -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        val payloadJson = serializer.serialize(payload)

        if (jsonRpcHistory.setRequest(payload.id, topic, payload.method, payloadJson)) {
            networkRepository.publish(topic, serializer.encode(payloadJson, topic), prompt) { result ->
                result.fold(
                    onSuccess = { onSuccess() },
                    onFailure = { error -> onFailure(error) }
                )
            }
        }
    }

    internal fun publishJsonRpcResponse(
        topic: TopicVO,
        response: JsonRpcResponseVO,
        onSuccess: () -> Unit = {},
        onFailure: (Throwable) -> Unit = {}
    ) {
        val responseJson = serializer.serialize(response.toRelayDOJsonRpcResponse())
        jsonRpcHistory.updateRequestWithResponse(response.id, responseJson)

        networkRepository.publish(topic, serializer.encode(responseJson, topic)) { result ->
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

    internal fun respondWithError(request: WCRequestVO, error: PeerError) {
        Logger.error("Responding with error: ${error.message}: ${error.code}")
        val jsonRpcError = JsonRpcResponseVO.JsonRpcError(id = request.id, error = JsonRpcResponseVO.Error(error.code, error.message))
        publishJsonRpcResponse(request.topic, jsonRpcError, onFailure = { failure -> Logger.error("Cannot respond with error: $failure") })
    }

    internal fun respondWithSuccess(request: WCRequestVO) {
        val jsonRpcResult = JsonRpcResponseVO.JsonRpcResult(id = request.id, result = "true")
        publishJsonRpcResponse(request.topic, jsonRpcResult,
            onFailure = { error -> Logger.error("Cannot send the response, error: $error") })
    }

    internal fun getPendingRequests(topic: TopicVO): List<PendingRequestVO> =
        jsonRpcHistory.getRequests(topic)
            .filter { entry -> entry.response == null && entry.method == JsonRpcMethod.WC_SESSION_PAYLOAD }
            .filter { entry -> serializer.tryDeserialize<PostSettlementSessionVO.SessionPayload>(entry.body) != null }
            .map { entry -> serializer.tryDeserialize<PostSettlementSessionVO.SessionPayload>(entry.body)!!.toPendingRequestVO(entry) }

    private fun manageSubscriptions() {
        scope.launch(exceptionHandler) {
            networkRepository.subscriptionRequest
                .map { relayRequest ->
                    val decodedMessage = serializer.decode(relayRequest.message, relayRequest.subscriptionTopic)
                    val topic = relayRequest.subscriptionTopic
                    Pair(decodedMessage, topic)
                }
                .collect { (decryptedMessage, topic) -> manageSubscriptions(decryptedMessage, topic) }
        }
    }

    private suspend fun manageSubscriptions(decryptedMessage: String, topic: TopicVO) {
        serializer.tryDeserialize<RelayDO.ClientJsonRpc>(decryptedMessage)?.let { clientJsonRpc ->
            handleRequest(clientJsonRpc, topic, decryptedMessage)
        } ?: serializer.tryDeserialize<RelayDO.JsonRpcResponse.JsonRpcResult>(decryptedMessage)?.let { result ->
            handleJsonRpcResult(result)
        } ?: serializer.tryDeserialize<RelayDO.JsonRpcResponse.JsonRpcError>(decryptedMessage)?.let { error ->
            handleJsonRpcError(error)
        } ?: Logger.error("WalletConnectRelay: Received unknown object type")
    }

    private suspend fun handleRequest(clientJsonRpc: RelayDO.ClientJsonRpc, topic: TopicVO, decryptedMessage: String) {
        if (jsonRpcHistory.setRequest(clientJsonRpc.id, topic, clientJsonRpc.method, decryptedMessage)) {
            serializer.deserialize(clientJsonRpc.method, decryptedMessage)?.let { params ->
                _clientSyncJsonRpc.emit(WCRequestVO(topic, clientJsonRpc.id, clientJsonRpc.method, params))
            } ?: Logger.error("WalletConnectRelay: Unknown request params")
        }
    }

    private suspend fun handleJsonRpcResult(jsonRpcResult: RelayDO.JsonRpcResponse.JsonRpcResult) {
        val jsonRpcRecord = jsonRpcHistory.updateRequestWithResponse(jsonRpcResult.id, serializer.serialize(jsonRpcResult))
        if (jsonRpcRecord != null) {
            serializer.deserialize(jsonRpcRecord.method, jsonRpcRecord.body)?.let { params ->
                _peerResponse.emit(jsonRpcRecord.toWCResponse(jsonRpcResult.toJsonRpcResultVO(), params))
            } ?: Logger.error("WalletConnectRelay: Unknown result params")
        }
    }

    private suspend fun handleJsonRpcError(jsonRpcError: RelayDO.JsonRpcResponse.JsonRpcError) {
        val jsonRpcRecord = jsonRpcHistory.updateRequestWithResponse(jsonRpcError.id, serializer.serialize(jsonRpcError))

        if (jsonRpcRecord != null) {
            serializer.deserialize(jsonRpcRecord.method, jsonRpcRecord.body)?.let { params ->
                _peerResponse.emit(jsonRpcRecord.toWCResponse(jsonRpcError.toJsonRpcErrorVO(), params))
            } ?: Logger.error("WalletConnectRelay: Unknown error params")
        }
    }

    private fun setOnConnectionOpen(event: WebSocket.Event) {
        if (event is WebSocket.Event.OnConnectionOpened<*>) {
            _isConnectionOpened.compareAndSet(expect = false, update = true)
        } else if (event is WebSocket.Event.OnConnectionClosed) {
            _isConnectionOpened.compareAndSet(expect = true, update = false)
        }
    }
}