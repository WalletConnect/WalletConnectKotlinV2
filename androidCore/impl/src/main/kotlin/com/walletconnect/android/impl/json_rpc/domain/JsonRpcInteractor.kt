package com.walletconnect.android.impl.json_rpc.domain

import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.exception.WalletConnectException
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.android.internal.common.exception.NoRelayConnectionException
import com.walletconnect.android.impl.common.model.sync.ClientJsonRpc
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.impl.crypto.Codec
import com.walletconnect.android.impl.json_rpc.data.JsonRpcSerializer
import com.walletconnect.android.impl.json_rpc.model.toJsonRpcError
import com.walletconnect.android.impl.json_rpc.model.toJsonRpcResponse
import com.walletconnect.android.impl.json_rpc.model.toRelay
import com.walletconnect.android.impl.json_rpc.model.toWCResponse
import com.walletconnect.android.impl.storage.JsonRpcHistory
import com.walletconnect.android.impl.utils.Logger
import com.walletconnect.android.internal.common.model.*
import com.walletconnect.android.relay.RelayConnectionInterface
import com.walletconnect.foundation.common.model.SubscriptionId
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.utils.Empty
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

internal class JsonRpcInteractor(
    private val relay: RelayConnectionInterface,
    private val chaChaPolyCodec: Codec,
    private val jsonRpcHistory: JsonRpcHistory,
): JsonRpcInteractorInterface {
    private val serializer: JsonRpcSerializer
        get() = wcKoinApp.koin.get()

    private val _clientSyncJsonRpc: MutableSharedFlow<WCRequest> = MutableSharedFlow()
    override val clientSyncJsonRpc: SharedFlow<WCRequest> = _clientSyncJsonRpc.asSharedFlow()

    private val _peerResponse: MutableSharedFlow<WCResponse> = MutableSharedFlow()
    override val peerResponse: SharedFlow<WCResponse> = _peerResponse.asSharedFlow()

    private val _internalErrors = MutableSharedFlow<InternalError>()
    override val internalErrors: SharedFlow<InternalError> = _internalErrors.asSharedFlow()

    override val isConnectionAvailable: StateFlow<Boolean> get() = relay.isConnectionAvailable

    private val subscriptions: MutableMap<String, String> = mutableMapOf()
    private val exceptionHandler = CoroutineExceptionHandler { _, exception -> handleError(exception.message ?: String.Empty) }

    override val initializationErrorsFlow: Flow<WalletConnectException> get() = relay.initializationErrorsFlow

    init {
        manageSubscriptions()
    }

    override fun checkConnectionWorking() {
        if (!relay.isConnectionAvailable.value) {
            throw NoRelayConnectionException("No connection available")
        }
    }

    override fun publishJsonRpcRequests(
        topic: Topic,
        params: IrnParams,
        payload: JsonRpcClientSync<*>,
        envelopeType: EnvelopeType,
        participants: Participants?,
        onSuccess: () -> Unit,
        onFailure: (Throwable) -> Unit,
    ) {
        checkConnectionWorking()
        val requestJson = serializer.serialize(payload) ?: return onFailure(IllegalStateException("JsonRpcInteractor: Unknown result params"))

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

    override fun publishJsonRpcResponse(
        topic: Topic,
        params: IrnParams,
        response: JsonRpcResponse,
        onSuccess: () -> Unit,
        onFailure: (Throwable) -> Unit,
        participants: Participants?,
        envelopeType: EnvelopeType,
    ) {
        checkConnectionWorking()

        val jsonResponseDO = response.toJsonRpcResponse()
        val responseJson = serializer.serialize(jsonResponseDO) ?: return onFailure(IllegalStateException("JsonRpcInteractor: Unknown result params"))
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

    override fun respondWithParams(
        request: WCRequest,
        clientParams: ClientParams,
        irnParams: IrnParams,
        envelopeType: EnvelopeType,
        participants: Participants?,
    ) {
        val result = JsonRpcResponse.JsonRpcResult(id = request.id, result = clientParams)

        publishJsonRpcResponse(request.topic, irnParams, result, envelopeType = envelopeType, participants = participants,
            onFailure = { error -> Logger.error("Cannot send the response, error: $error") })
    }

    override fun respondWithSuccess(
        request: WCRequest,
        irnParams: IrnParams,
        envelopeType: EnvelopeType,
        participants: Participants?,
    ) {
        val result = JsonRpcResponse.JsonRpcResult(id = request.id, result = true)

        try {
            publishJsonRpcResponse(request.topic, irnParams, result, envelopeType = envelopeType, participants = participants,
                onFailure = { error -> Logger.error("Cannot send the response, error: $error") })
        } catch (e: Exception) {
            handleError(e.message ?: String.Empty)
        }
    }

    override fun respondWithError(
        request: WCRequest,
        error: Error,
        irnParams: IrnParams,
        envelopeType: EnvelopeType,
        participants: Participants?,
        onFailure: (Throwable) -> Unit,
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

    override fun subscribe(topic: Topic) {
        checkConnectionWorking()
        relay.subscribe(topic.value) { result ->
            result.fold(
                onSuccess = { acknowledgement -> subscriptions[topic.value] = acknowledgement.result },
                onFailure = { error -> Logger.error("Subscribe to topic error: $topic error: $error") }
            )
        }
    }

    override fun unsubscribe(topic: Topic) {
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
                }.collect { (decryptedMessage, topic) -> manageSubscriptions(decryptedMessage, topic) }
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
        val serializedResult = serializer.serialize(jsonRpcResult) ?: return handleError("JsonRpcInteractor: Unknown result params")
        val jsonRpcRecord = jsonRpcHistory.updateRequestWithResponse(jsonRpcResult.id, serializedResult)

        if (jsonRpcRecord != null) {
            serializer.deserialize(jsonRpcRecord.method, jsonRpcRecord.body)?.let { params ->
                val responseVO = JsonRpcResponse.JsonRpcResult(jsonRpcResult.id, result = jsonRpcResult.result)
                _peerResponse.emit(jsonRpcRecord.toWCResponse(responseVO, params))
            } ?: handleError("JsonRpcInteractoraklsfhj: Unknown result params")
        }
    }

    private suspend fun handleJsonRpcError(jsonRpcError: JsonRpcResponse.JsonRpcError) {
        val serializedResult = serializer.serialize(jsonRpcError) ?: return handleError("JsonRpcInteractor: Unknown result params")
        val jsonRpcRecord = jsonRpcHistory.updateRequestWithResponse(jsonRpcError.id, serializedResult)

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