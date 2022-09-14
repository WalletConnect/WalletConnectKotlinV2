package com.walletconnect.android.impl.json_rpc.domain

import com.walletconnect.android.RelayConnectionInterface
import com.walletconnect.android.common.JsonRpcResponse
import com.walletconnect.android.exception.NoRelayConnectionException
import com.walletconnect.android.exception.WalletConnectException
import com.walletconnect.android.impl.common.model.IrnParams
import com.walletconnect.android.impl.common.model.sync.WCRequest
import com.walletconnect.android.impl.common.model.sync.WCResponse
import com.walletconnect.android.impl.common.model.type.ClientParams
import com.walletconnect.android.impl.common.model.type.Error
import com.walletconnect.android.impl.common.model.type.JsonRpcClientSync
import com.walletconnect.android.impl.common.model.type.enums.EnvelopeType
import com.walletconnect.android.impl.common.scope.scope
import com.walletconnect.android.impl.crypto.Codec
import com.walletconnect.android.impl.json_rpc.data.JsonRpcSerializerAbstract
import com.walletconnect.android.impl.json_rpc.model.*
import com.walletconnect.android.impl.common.model.Participants
import com.walletconnect.android.impl.common.model.sync.ClientJsonRpc
import com.walletconnect.android.impl.storage.JsonRpcHistory
import com.walletconnect.android.impl.utils.Logger
import com.walletconnect.foundation.common.model.SubscriptionId
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.utils.Empty
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.lang.InternalError

open class BaseJsonRpcInteractor(
    private val relay: RelayConnectionInterface,
    private val serializer: JsonRpcSerializerAbstract,
    private val chaChaPolyCodec: Codec,
    private val jsonRpcHistory: JsonRpcHistory,
) {
    private val _clientSyncJsonRpc: MutableSharedFlow<WCRequest> = MutableSharedFlow()
    val clientSyncJsonRpc: SharedFlow<WCRequest> = _clientSyncJsonRpc.asSharedFlow()

    private val _peerResponse: MutableSharedFlow<WCResponse> = MutableSharedFlow()
    val peerResponse: SharedFlow<WCResponse> = _peerResponse.asSharedFlow()

    private val _internalErrors = MutableSharedFlow<InternalError>()
    val internalErrors: SharedFlow<InternalError> = _internalErrors.asSharedFlow()

    val isConnectionAvailable: StateFlow<Boolean> get() = relay.isConnectionAvailable

    private val subscriptions: MutableMap<String, String> = mutableMapOf()
    private val exceptionHandler = CoroutineExceptionHandler { _, exception -> handleError(exception.message ?: String.Empty) }

    val initializationErrorsFlow: Flow<WalletConnectException> get() = relay.initializationErrorsFlow

    init {
        manageSubscriptions()
    }

    fun checkConnectionWorking() {
        if (!relay.isConnectionAvailable.value) {
            throw NoRelayConnectionException("No connection available")
        }
    }

    fun publishJsonRpcRequests(
        topic: Topic,
        params: IrnParams,
        payload: JsonRpcClientSync<*>,
        envelopeType: EnvelopeType = EnvelopeType.ZERO,
        participants: Participants? = null,
        onSuccess: () -> Unit = {},
        onFailure: (Throwable) -> Unit = {},
    ) {
        checkConnectionWorking()
        val requestJson = serializer.serialize(payload)

        if (jsonRpcHistory.setRequest(payload.id, topic, payload.method, requestJson)) {
            val encryptedRequest = chaChaPolyCodec.encrypt(topic, requestJson, envelopeType, participants)

            relay.publish(topic.value, encryptedRequest, params.toRelay()) { result ->
                result.fold(
                    onSuccess = { onSuccess() },
                    onFailure = { error -> onFailure(error) }
                )
            }
        }
    }

    fun publishJsonRpcResponse(
        topic: Topic,
        params: IrnParams,
        response: JsonRpcResponse,
        onSuccess: () -> Unit = {},
        onFailure: (Throwable) -> Unit = {},
        participants: Participants? = null,
        envelopeType: EnvelopeType = EnvelopeType.ZERO,
        ) {
        checkConnectionWorking()

        val jsonResponseDO = response.toJsonRpcResponse()
        val responseJson = serializer.serialize(jsonResponseDO)
        val encryptedResponse = chaChaPolyCodec.encrypt(topic, responseJson, envelopeType, participants)

        relay.publish(topic.value, encryptedResponse, params.toRelay()) { result ->
            result.fold(
                onSuccess = {
                    jsonRpcHistory.updateRequestWithResponse(response.id, responseJson)
                    onSuccess()
                },
                onFailure = { error -> onFailure(error) }
            )
        }
    }

    fun respondWithParams(
        request: WCRequest,
        clientParams: ClientParams,
        irnParams: IrnParams,
        envelopeType: EnvelopeType = EnvelopeType.ZERO,
        participants: Participants? = null,

        ) {
        val result = JsonRpcResponse.JsonRpcResult(id = request.id, result = clientParams)

        publishJsonRpcResponse(request.topic, irnParams, result, envelopeType = envelopeType, participants = participants,
            onFailure = { error -> Logger.error("Cannot send the response, error: $error") })
    }

    fun respondWithSuccess(
        request: WCRequest,
        irnParams: IrnParams,
        envelopeType: EnvelopeType = EnvelopeType.ZERO,
        participants: Participants? = null,
        ) {
        val result = JsonRpcResponse.JsonRpcResult(id = request.id, result = true)

        try {
            publishJsonRpcResponse(request.topic, irnParams, result, envelopeType = envelopeType, participants = participants,
                onFailure = { error -> Logger.error("Cannot send the response, error: $error") })
        } catch (e: Exception) {
            handleError(e.message ?: String.Empty)
        }
    }

    fun respondWithError(
        request: WCRequest,
        error: Error,
        irnParams: IrnParams,
        envelopeType: EnvelopeType = EnvelopeType.ZERO,
        participants: Participants? = null,
        onFailure: (Throwable) -> Unit = {},
    ) {
        Logger.error("Responding with error: ${error.message}: ${error.code}")
        val jsonRpcError = JsonRpcResponse.JsonRpcError(id = request.id, error = JsonRpcResponse.Error(error.code, error.message))

        try {
            publishJsonRpcResponse(request.topic, irnParams, jsonRpcError, envelopeType = envelopeType, participants = participants,
                onFailure = { failure ->
                    Logger.error("Cannot respond with error: $failure")
                    onFailure(failure)
                })
        } catch (e: Exception) {
            handleError(e.message ?: String.Empty)
        }
    }

    fun subscribe(topic: Topic) {
        checkConnectionWorking()
        relay.subscribe(topic.value) { result ->
            result.fold(
                onSuccess = { acknowledgement -> subscriptions[topic.value] = acknowledgement.result },
                onFailure = { error -> Logger.error("Subscribe to topic error: $topic error: $error") }
            )
        }
    }

    fun unsubscribe(topic: Topic) {
        checkConnectionWorking()
        if (subscriptions.contains(topic.value)) {
            val subscriptionId = SubscriptionId(subscriptions[topic.value].toString())
            relay.unsubscribe(topic.value, subscriptionId.id) { result ->
                result.fold(
                    onSuccess = {
                        jsonRpcHistory.deleteRecordsByTopic(topic)
                        subscriptions.remove(topic.value)
                    },
                    onFailure = { error -> Logger.error("Unsubscribe to topic: $topic error: $error") }
                )
            }
        }
    }

    private fun manageSubscriptions() {
        scope.launch(exceptionHandler) {
            relay.subscriptionRequest
                .map { relayRequest ->
                    val topic = Topic(relayRequest.subscriptionTopic)
                    val message = chaChaPolyCodec.decrypt(topic, relayRequest.message)

                    Pair(message, topic)
                }
                .collect { (decryptedMessage, topic) -> manageSubscriptions(decryptedMessage, topic) }
        }
    }

    private suspend fun manageSubscriptions(decryptedMessage: String, topic: Topic) {
        serializer.tryDeserialize<ClientJsonRpc>(decryptedMessage)?.let { clientJsonRpc ->
            handleRequest(clientJsonRpc, topic, decryptedMessage)
        } ?: serializer.tryDeserialize<JsonRpcResponse.JsonRpcResult>(decryptedMessage)?.let { result ->
            handleJsonRpcResult(result)
        } ?: serializer.tryDeserialize<JsonRpcResponse.JsonRpcError>(decryptedMessage)?.let { error ->
            handleJsonRpcError(error)
        } ?: handleError("JsonRpcInteractor: Received unknown object type")
    }

    private suspend fun handleRequest(clientJsonRpc: ClientJsonRpc, topic: Topic, decryptedMessage: String) {
        if (jsonRpcHistory.setRequest(clientJsonRpc.id, topic, clientJsonRpc.method, decryptedMessage)) {
            serializer.deserialize(clientJsonRpc.method, decryptedMessage)?.let { params ->
                _clientSyncJsonRpc.emit(WCRequest(topic, clientJsonRpc.id, clientJsonRpc.method, params))
            } ?: handleError("JsonRpcInteractor: Unknown request params")
        }
    }

    private suspend fun handleJsonRpcResult(jsonRpcResult: JsonRpcResponse.JsonRpcResult) {
        val jsonRpcRecord = jsonRpcHistory.updateRequestWithResponse(jsonRpcResult.id, serializer.serialize(jsonRpcResult))

        if (jsonRpcRecord != null) {
            serializer.deserialize(jsonRpcRecord.method, jsonRpcRecord.body)?.let { params ->
                val responseVO = JsonRpcResponse.JsonRpcResult(jsonRpcResult.id, result = jsonRpcResult.result)
                _peerResponse.emit(jsonRpcRecord.toWCResponse(responseVO, params))
            } ?: handleError("JsonRpcInteractor: Unknown result params")
        }
    }

    private suspend fun handleJsonRpcError(jsonRpcError: JsonRpcResponse.JsonRpcError) {
        val jsonRpcRecord = jsonRpcHistory.updateRequestWithResponse(jsonRpcError.id, serializer.serialize(jsonRpcError))

        if (jsonRpcRecord != null) {
            serializer.deserialize(jsonRpcRecord.method, jsonRpcRecord.body)?.let { params ->
                _peerResponse.emit(jsonRpcRecord.toWCResponse(jsonRpcError.toJsonRpcError(), params))
            } ?: handleError("JsonRpcInteractor: Unknown error params")
        }
    }

    private fun handleError(errorMessage: String) {
        Logger.error(errorMessage)
        scope.launch {
            _internalErrors.emit(InternalError(errorMessage))
        }
    }
}