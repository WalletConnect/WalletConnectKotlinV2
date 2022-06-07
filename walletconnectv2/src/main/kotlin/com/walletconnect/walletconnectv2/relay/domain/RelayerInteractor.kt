package com.walletconnect.walletconnectv2.relay.domain

import com.walletconnect.walletconnectv2.client.Sign
import com.walletconnect.walletconnectv2.core.exceptions.client.WalletConnectException
import com.walletconnect.walletconnectv2.core.exceptions.peer.PeerError
import com.walletconnect.walletconnectv2.core.model.type.ClientParams
import com.walletconnect.walletconnectv2.core.model.type.SettlementSequence
import com.walletconnect.walletconnectv2.core.model.utils.JsonRpcMethod
import com.walletconnect.walletconnectv2.core.model.vo.SubscriptionIdVO
import com.walletconnect.walletconnectv2.core.model.vo.TopicVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.SessionSettlementVO
import com.walletconnect.walletconnectv2.core.model.vo.jsonRpc.JsonRpcResponseVO
import com.walletconnect.walletconnectv2.core.model.vo.sync.PendingRequestVO
import com.walletconnect.walletconnectv2.core.model.vo.sync.WCRequestVO
import com.walletconnect.walletconnectv2.core.model.vo.sync.WCResponseVO
import com.walletconnect.walletconnectv2.core.scope.scope
import com.walletconnect.walletconnectv2.network.Relay
import com.walletconnect.walletconnectv2.relay.data.serializer.JsonRpcSerializer
import com.walletconnect.walletconnectv2.relay.model.*
import com.walletconnect.walletconnectv2.storage.history.JsonRpcHistory
import com.walletconnect.walletconnectv2.util.Logger
import com.walletconnect.walletconnectv2.util.NetworkState
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.net.HttpURLConnection

internal class RelayerInteractor(
    private val relay: Relay,
    private val serializer: JsonRpcSerializer,
    private val jsonRpcHistory: JsonRpcHistory,
    networkState: NetworkState,
) {
    private val _clientSyncJsonRpc: MutableSharedFlow<WCRequestVO> = MutableSharedFlow()
    internal val clientSyncJsonRpc: SharedFlow<WCRequestVO> = _clientSyncJsonRpc

    private val _peerResponse: MutableSharedFlow<WCResponseVO> = MutableSharedFlow()
    val peerResponse: SharedFlow<WCResponseVO> = _peerResponse

    private val _isNetworkAvailable: StateFlow<Boolean> = networkState.isAvailable
    private val _isWSSConnectionOpened: MutableStateFlow<Boolean> = MutableStateFlow(false)

    val isConnectionAvailable: StateFlow<Boolean> =
        combine(_isWSSConnectionOpened, _isNetworkAvailable) { wss, internet -> wss && internet }
            .stateIn(scope, SharingStarted.Eagerly, false)

    private val subscriptions: MutableMap<String, String> = mutableMapOf()
    private val exceptionHandler = CoroutineExceptionHandler { _, exception -> Logger.error("Exception handler: $exception") }

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
        get() = relay.eventsFlow
            .onEach { event: Sign.Model.Relay.Event ->
                Logger.log("$event")
                setIsWSSConnectionOpened(event)
            }
            .filterIsInstance<Sign.Model.Relay.Event.OnConnectionFailed>()
            .map { error -> error.throwable.toWalletConnectException }

    init {
        manageSubscriptions()
    }

    internal fun checkConnectionWorking() {
        if (!isConnectionAvailable.value) {
            throw WalletConnectException.MissingInternetConnectionException("No connection available")
        }
    }

    internal fun publishJsonRpcRequests(
        topic: TopicVO,
        payload: SettlementSequence<*>,
        onSuccess: () -> Unit = {},
        onFailure: (Throwable) -> Unit = {},
    ) {
        checkConnectionWorking()
        val requestJson = serializer.serialize(payload)

        if (jsonRpcHistory.setRequest(payload.id, topic, payload.method, requestJson)) {
            val encodedRequest = serializer.encrypt(requestJson, topic)
            relay.publish(topic.value, encodedRequest, shouldPrompt(payload.method)) { result ->
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
        onFailure: (Throwable) -> Unit = {},
    ) {
        checkConnectionWorking()
        val jsonResponseDO = response.toRelayerDOJsonRpcResponse()
        val responseJson = serializer.serialize(jsonResponseDO)
        val encodedJson = serializer.encrypt(responseJson, topic)

        relay.publish(topic.value, encodedJson) { result ->
            result.fold(
                onSuccess = {
                    jsonRpcHistory.updateRequestWithResponse(response.id, responseJson)
                    onSuccess()
                },
                onFailure = { error -> onFailure(error) }
            )
        }
    }

    internal fun respondWithParams(request: WCRequestVO, params: ClientParams) {
        val result = JsonRpcResponseVO.JsonRpcResult(id = request.id, result = params)
        publishJsonRpcResponse(request.topic, result, onFailure = { error -> Logger.error("Cannot send the response, error: $error") })
    }

    internal fun respondWithSuccess(request: WCRequestVO) {
        val result = JsonRpcResponseVO.JsonRpcResult(id = request.id, result = true)
        publishJsonRpcResponse(request.topic, result, onFailure = { error -> Logger.error("Cannot send the response, error: $error") })
    }

    internal fun respondWithError(request: WCRequestVO, error: PeerError, onFailure: (Throwable) -> Unit = {}) {
        Logger.error("Responding with error: ${error.message}: ${error.code}")
        val jsonRpcError = JsonRpcResponseVO.JsonRpcError(id = request.id, error = JsonRpcResponseVO.Error(error.code, error.message))

        publishJsonRpcResponse(request.topic, jsonRpcError,
            onFailure = { failure ->
                Logger.error("Cannot respond with error: $failure")
                onFailure(failure)
            })
    }

    internal fun subscribe(topic: TopicVO) {
        checkConnectionWorking()
        relay.subscribe(topic.value) { result ->
            result.fold(
                onSuccess = { acknowledgement -> subscriptions[topic.value] = acknowledgement.result },
                onFailure = { error -> Logger.error("Subscribe to topic: $topic error: $error") }
            )
        }
    }

    internal fun unsubscribe(topic: TopicVO) {
        checkConnectionWorking()
        if (subscriptions.contains(topic.value)) {
            val subscriptionId = SubscriptionIdVO(subscriptions[topic.value].toString())
            relay.unsubscribe(topic.value, subscriptionId.id) { result ->
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

    internal fun getPendingRequests(topic: TopicVO): List<PendingRequestVO> =
        jsonRpcHistory.getRequests(topic)
            .filter { entry -> entry.response == null && entry.method == JsonRpcMethod.WC_SESSION_REQUEST }
            .filter { entry -> serializer.tryDeserialize<SessionSettlementVO.SessionRequest>(entry.body) != null }
            .map { entry -> serializer.tryDeserialize<SessionSettlementVO.SessionRequest>(entry.body)!!.toPendingRequestVO(entry) }

    private fun manageSubscriptions() {
        scope.launch(exceptionHandler) {
            relay.subscriptionRequest
                .map { relayRequest ->
                    val topic = TopicVO(relayRequest.subscriptionTopic)
                    val decodedMessage = serializer.decrypt(relayRequest.message, topic)

                    Pair(decodedMessage, topic)
                }
                .collect { (decryptedMessage, topic) -> manageSubscriptions(decryptedMessage, topic) }
        }
    }

    private suspend fun manageSubscriptions(decryptedMessage: String, topic: TopicVO) {
        serializer.tryDeserialize<RelayerDO.ClientJsonRpc>(decryptedMessage)?.let { clientJsonRpc ->
            handleRequest(clientJsonRpc, topic, decryptedMessage)
        } ?: serializer.tryDeserialize<RelayerDO.JsonRpcResponse.JsonRpcResult>(decryptedMessage)?.let { result ->
            handleJsonRpcResult(result)
        } ?: serializer.tryDeserialize<RelayerDO.JsonRpcResponse.JsonRpcError>(decryptedMessage)?.let { error ->
            handleJsonRpcError(error)
        } ?: Logger.error("RelayerInteractor: Received unknown object type")
    }

    private suspend fun handleRequest(clientJsonRpc: RelayerDO.ClientJsonRpc, topic: TopicVO, decryptedMessage: String) {
        if (jsonRpcHistory.setRequest(clientJsonRpc.id, topic, clientJsonRpc.method, decryptedMessage)) {
            serializer.deserialize(clientJsonRpc.method, decryptedMessage)?.let { params ->
                _clientSyncJsonRpc.emit(WCRequestVO(topic, clientJsonRpc.id, clientJsonRpc.method, params))
            } ?: Logger.error("RelayerInteractor: Unknown request params")
        }
    }

    private suspend fun handleJsonRpcResult(jsonRpcResult: RelayerDO.JsonRpcResponse.JsonRpcResult) {
        val jsonRpcRecord = jsonRpcHistory.updateRequestWithResponse(jsonRpcResult.id, serializer.serialize(jsonRpcResult))

        if (jsonRpcRecord != null) {
            serializer.deserialize(jsonRpcRecord.method, jsonRpcRecord.body)?.let { params ->
                val responseVO = JsonRpcResponseVO.JsonRpcResult(jsonRpcResult.id, result = jsonRpcResult.result)
                _peerResponse.emit(jsonRpcRecord.toWCResponse(responseVO, params))
            } ?: Logger.error("RelayerInteractor: Unknown result params")
        }
    }

    private suspend fun handleJsonRpcError(jsonRpcError: RelayerDO.JsonRpcResponse.JsonRpcError) {
        val jsonRpcRecord = jsonRpcHistory.updateRequestWithResponse(jsonRpcError.id, serializer.serialize(jsonRpcError))

        if (jsonRpcRecord != null) {
            serializer.deserialize(jsonRpcRecord.method, jsonRpcRecord.body)?.let { params ->
                _peerResponse.emit(jsonRpcRecord.toWCResponse(jsonRpcError.toJsonRpcErrorVO(), params))
            } ?: Logger.error("RelayerInteractor: Unknown error params")
        }
    }

    private fun setIsWSSConnectionOpened(event: Sign.Model.Relay.Event) {
        if (event is Sign.Model.Relay.Event.OnConnectionOpened<*>) {
            _isWSSConnectionOpened.compareAndSet(expect = false, update = true)
        } else if (event is Sign.Model.Relay.Event.OnConnectionClosed || event is Sign.Model.Relay.Event.OnConnectionFailed) {
            _isWSSConnectionOpened.compareAndSet(expect = true, update = false)
        }
    }

    private fun shouldPrompt(method: String): Boolean = method == JsonRpcMethod.WC_SESSION_REQUEST || method == JsonRpcMethod.WC_SESSION_PROPOSE
}