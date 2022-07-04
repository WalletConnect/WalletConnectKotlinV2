@file:JvmSynthetic

package com.walletconnect.chat.copiedFromSign.json_rpc.domain

import com.walletconnect.chat.copiedFromSign.core.exceptions.client.WalletConnectException
import com.walletconnect.chat.copiedFromSign.core.exceptions.peer.PeerError
import com.walletconnect.chat.copiedFromSign.core.model.client.Relay
import com.walletconnect.chat.copiedFromSign.core.model.type.ClientParams
import com.walletconnect.chat.copiedFromSign.core.model.type.JsonRpcClientSync
import com.walletconnect.chat.copiedFromSign.core.model.type.enums.EnvelopeType
import com.walletconnect.chat.copiedFromSign.core.model.vo.SubscriptionIdVO
import com.walletconnect.chat.copiedFromSign.core.model.vo.TopicVO
import com.walletconnect.chat.copiedFromSign.core.model.vo.jsonRpc.JsonRpcResponseVO
import com.walletconnect.chat.copiedFromSign.core.model.vo.sync.ParticipantsVO
import com.walletconnect.chat.copiedFromSign.core.model.vo.sync.WCRequestVO
import com.walletconnect.chat.copiedFromSign.core.model.vo.sync.WCResponseVO
import com.walletconnect.chat.copiedFromSign.core.scope.scope
import com.walletconnect.chat.copiedFromSign.crypto.Codec
import com.walletconnect.chat.copiedFromSign.json_rpc.data.JsonRpcSerializer
import com.walletconnect.chat.copiedFromSign.json_rpc.model.RelayerDO
import com.walletconnect.chat.copiedFromSign.json_rpc.model.toJsonRpcErrorVO
import com.walletconnect.chat.copiedFromSign.json_rpc.model.toRelayerDOJsonRpcResponse
import com.walletconnect.chat.copiedFromSign.json_rpc.model.toWCResponse
import com.walletconnect.chat.copiedFromSign.network.RelayInterface
import com.walletconnect.chat.copiedFromSign.storage.JsonRpcHistory
import com.walletconnect.chat.copiedFromSign.util.Empty
import com.walletconnect.chat.copiedFromSign.util.Logger
import com.walletconnect.chat.copiedFromSign.util.NetworkState
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.net.HttpURLConnection

//todo: extract Relay to core module. Consider what is the best place for RelayerInteractor (sdk or core module)?
internal class RelayerInteractor(
    private val relay: RelayInterface,
    private val serializer: JsonRpcSerializer,
    private val chaChaPolyCodec: Codec,
    private val jsonRpcHistory: JsonRpcHistory,
    networkState: NetworkState, //todo: move to the RelayClient?
) {
    private val _clientSyncJsonRpc: MutableSharedFlow<WCRequestVO> = MutableSharedFlow()
    val clientSyncJsonRpc: SharedFlow<WCRequestVO> = _clientSyncJsonRpc.asSharedFlow()

    private val _peerResponse: MutableSharedFlow<WCResponseVO> = MutableSharedFlow()
    val peerResponse: SharedFlow<WCResponseVO> = _peerResponse.asSharedFlow()

    private val _internalErrors = MutableSharedFlow<WalletConnectException.InternalError>()
    val internalErrors: SharedFlow<WalletConnectException.InternalError> = _internalErrors.asSharedFlow()

    private val _isNetworkAvailable: StateFlow<Boolean> = networkState.isAvailable
    private val _isWSSConnectionOpened: MutableStateFlow<Boolean> = MutableStateFlow(false)

    private val subscriptions: MutableMap<String, String> = mutableMapOf()
    private val exceptionHandler = CoroutineExceptionHandler { _, exception -> handleError(exception.message ?: String.Empty) }

    init {
        manageSubscriptions()
    }

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
            .onEach { event: Relay.Model.Event ->
                Logger.log("$event")
                setIsWSSConnectionOpened(event)
            }
            .filterIsInstance<Relay.Model.Event.OnConnectionFailed>()
            .map { error -> error.throwable.toWalletConnectException }

    internal fun publishJsonRpcRequests(
        topic: TopicVO,
        payload: JsonRpcClientSync<*>,
        envelopeType: EnvelopeType,
        onSuccess: () -> Unit = {},
        onFailure: (Throwable) -> Unit = {},
        participantsVO: ParticipantsVO? = null
    ) {
        val requestJson = serializer.serialize(payload)
        if (jsonRpcHistory.setRequest(payload.id, topic, payload.method, requestJson)) {
            val encryptedRequest = chaChaPolyCodec.encrypt(topic, requestJson, envelopeType, participantsVO)

            //todo when promp should be true for chat sdk?
            relay.publish(topic.value, encryptedRequest, true) { result ->
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
        envelopeType: EnvelopeType,
        onSuccess: () -> Unit = {},
        onFailure: (Throwable) -> Unit = {},
    ) {

        val jsonResponseDO = response.toRelayerDOJsonRpcResponse()
        val responseJson = serializer.serialize(jsonResponseDO)
        val encryptedResponse = chaChaPolyCodec.encrypt(topic, responseJson, envelopeType)

        relay.publish(topic.value, encryptedResponse) { result ->
            result.fold(
                onSuccess = {
                    jsonRpcHistory.updateRequestWithResponse(response.id, responseJson)
                    onSuccess()
                },
                onFailure = { error -> onFailure(error) }
            )
        }
    }

    internal fun respondWithParams(request: WCRequestVO, params: ClientParams, envelopeType: EnvelopeType) {
        val result = JsonRpcResponseVO.JsonRpcResult(id = request.id, result = params)
        publishJsonRpcResponse(request.topic, result, envelopeType, onFailure = { error ->
            Logger.error("Cannot send the response, error: $error")
        })
    }

    internal fun respondWithSuccess(request: WCRequestVO, envelopeType: EnvelopeType) {
        val result = JsonRpcResponseVO.JsonRpcResult(id = request.id, result = true)
        try {
            publishJsonRpcResponse(request.topic, result, envelopeType, onFailure = { error ->
                Logger.error("Cannot send the response, error: $error")
            })
        } catch (e: Exception) {
            handleError(e.message ?: String.Empty)
        }
    }

    internal fun respondWithError(
        request: WCRequestVO,
        error: PeerError,
        envelopeType: EnvelopeType,
        onFailure: (Throwable) -> Unit = {},
    ) {
        Logger.error("Responding with error: ${error.message}: ${error.code}")
        val jsonRpcError = JsonRpcResponseVO.JsonRpcError(id = request.id, error = JsonRpcResponseVO.Error(error.code, error.message))

        try {
            publishJsonRpcResponse(request.topic, jsonRpcError, envelopeType, onFailure = { failure ->
                Logger.error("Cannot respond with error: $failure")
                onFailure(failure)
            })
        } catch (e: Exception) {
            handleError(e.message ?: String.Empty)
        }
    }

    internal fun subscribe(topic: TopicVO) {
        relay.subscribe(topic.value) { result ->
            result.fold(
                onSuccess = { acknowledgement -> subscriptions[topic.value] = acknowledgement.result },
                onFailure = { error -> Logger.error("Subscribe to topic: $topic error: $error") }
            )
        }
    }

    internal fun unsubscribe(topic: TopicVO) {
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

//        internal fun getMessages(topic: TopicVO): List<ChatMessageVO> =
//        jsonRpcHistory.getRequests(topic)
//            .filter { entry -> entry.response != null && entry.method == JsonRpcMethod.WC_CHAT_MESSAGE }
////            .filter { entry -> serializer.tryDeserialize<SessionSettlementVO.SessionRequest>(entry.body) != null }
////            .map { entry -> serializer.tryDeserialize<SessionSettlementVO.SessionRequest>(entry.body)!!.toPendingRequestVO(entry) }

    private fun manageSubscriptions() {
        scope.launch(exceptionHandler) {
            relay.subscriptionRequest
                .map { relayRequest ->
                    val topic = TopicVO(relayRequest.subscriptionTopic)
                    val message = chaChaPolyCodec.decrypt(topic, relayRequest.message)

                    Pair(message, topic)
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
        } ?: handleError("RelayerInteractor: Received unknown object type")
    }

    private suspend fun handleRequest(clientJsonRpc: RelayerDO.ClientJsonRpc, topic: TopicVO, decryptedMessage: String) {
        if (jsonRpcHistory.setRequest(clientJsonRpc.id, topic, clientJsonRpc.method, decryptedMessage)) {
            serializer.deserialize(clientJsonRpc.method, decryptedMessage)?.let { params ->
                _clientSyncJsonRpc.emit(WCRequestVO(topic, clientJsonRpc.id, clientJsonRpc.method, params))
            } ?: handleError("RelayerInteractor: Unknown request params")
        }
    }

    private suspend fun handleJsonRpcResult(jsonRpcResult: RelayerDO.JsonRpcResponse.JsonRpcResult) {
        val jsonRpcRecord = jsonRpcHistory.updateRequestWithResponse(jsonRpcResult.id, serializer.serialize(jsonRpcResult))

        if (jsonRpcRecord != null) {
            serializer.deserialize(jsonRpcRecord.method, jsonRpcRecord.body)?.let { params ->
                val responseVO = JsonRpcResponseVO.JsonRpcResult(jsonRpcResult.id, result = jsonRpcResult.result)
                _peerResponse.emit(jsonRpcRecord.toWCResponse(responseVO, params))
            } ?: handleError("RelayerInteractor: Unknown result params")
        }
    }

    private suspend fun handleJsonRpcError(jsonRpcError: RelayerDO.JsonRpcResponse.JsonRpcError) {
        val jsonRpcRecord = jsonRpcHistory.updateRequestWithResponse(jsonRpcError.id, serializer.serialize(jsonRpcError))

        if (jsonRpcRecord != null) {
            serializer.deserialize(jsonRpcRecord.method, jsonRpcRecord.body)?.let { params ->
                _peerResponse.emit(jsonRpcRecord.toWCResponse(jsonRpcError.toJsonRpcErrorVO(), params))
            } ?: handleError("RelayerInteractor: Unknown error params")
        }
    }

    private fun setIsWSSConnectionOpened(event: Relay.Model.Event) {
        if (event is Relay.Model.Event.OnConnectionOpened<*>) {
            _isWSSConnectionOpened.compareAndSet(expect = false, update = true)
        } else if (event is Relay.Model.Event.OnConnectionClosed || event is Relay.Model.Event.OnConnectionFailed) {
            _isWSSConnectionOpened.compareAndSet(expect = true, update = false)
        }
    }

    private fun handleError(errorMessage: String) {
        Logger.error(errorMessage)
        scope.launch {
            _internalErrors.emit(WalletConnectException.InternalError(errorMessage))
        }
    }
}