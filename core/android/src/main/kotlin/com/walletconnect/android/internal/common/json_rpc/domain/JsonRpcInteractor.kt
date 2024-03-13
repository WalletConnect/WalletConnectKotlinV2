package com.walletconnect.android.internal.common.json_rpc.domain

import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.crypto.codec.Codec
import com.walletconnect.android.internal.common.crypto.sha256
import com.walletconnect.android.internal.common.exception.NoRelayConnectionException
import com.walletconnect.android.internal.common.exception.Uncategorized
import com.walletconnect.android.internal.common.json_rpc.data.JsonRpcSerializer
import com.walletconnect.android.internal.common.json_rpc.model.toRelay
import com.walletconnect.android.internal.common.json_rpc.model.toWCResponse
import com.walletconnect.android.internal.common.model.EnvelopeType
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.Participants
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.model.WCRequest
import com.walletconnect.android.internal.common.model.WCResponse
import com.walletconnect.android.internal.common.model.params.ChatNotifyResponseAuthParams
import com.walletconnect.android.internal.common.model.sync.ClientJsonRpc
import com.walletconnect.android.internal.common.model.type.ClientParams
import com.walletconnect.android.internal.common.model.type.Error
import com.walletconnect.android.internal.common.model.type.JsonRpcClientSync
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.storage.push_messages.PushMessagesRepository
import com.walletconnect.android.internal.common.storage.rpc.JsonRpcHistory
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.android.relay.RelayConnectionInterface
import com.walletconnect.foundation.common.model.SubscriptionId
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.network.model.Relay
import com.walletconnect.foundation.util.Logger
import com.walletconnect.utils.Empty
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

internal class JsonRpcInteractor(
    private val relay: RelayConnectionInterface,
    private val chaChaPolyCodec: Codec,
    private val jsonRpcHistory: JsonRpcHistory,
    private val pushMessageStorage: PushMessagesRepository,
    private val logger: Logger,
) : JsonRpcInteractorInterface {
    private val serializer: JsonRpcSerializer get() = wcKoinApp.koin.get()

    private val _clientSyncJsonRpc: MutableSharedFlow<WCRequest> = MutableSharedFlow()
    override val clientSyncJsonRpc: SharedFlow<WCRequest> = _clientSyncJsonRpc.asSharedFlow()

    private val _peerResponse: MutableSharedFlow<WCResponse> = MutableSharedFlow()
    override val peerResponse: SharedFlow<WCResponse> = _peerResponse.asSharedFlow()

    private val _internalErrors = MutableSharedFlow<SDKError>()
    override val internalErrors: SharedFlow<SDKError> = _internalErrors.asSharedFlow()

    override val isConnectionAvailable: StateFlow<Boolean> get() = relay.isConnectionAvailable

    private val subscriptions: MutableMap<String, String> = mutableMapOf()

    init {
        manageSubscriptions()
    }

    override fun checkConnectionWorking() {
        if (!relay.isConnectionAvailable.value) {
            throw NoRelayConnectionException("Connection error: Please check your Internet connection")
        }
    }

    override fun publishJsonRpcRequest(
        topic: Topic,
        params: IrnParams,
        payload: JsonRpcClientSync<*>,
        envelopeType: EnvelopeType,
        participants: Participants?,
        onSuccess: () -> Unit,
        onFailure: (Throwable) -> Unit,
    ) {
        try {
            checkConnectionWorking()
        } catch (e: NoRelayConnectionException) {
            return onFailure(e)
        }

        val requestJson = try {
            serializer.serialize(payload) ?: return onFailure(IllegalStateException("JsonRpcInteractor: Unknown result params"))
        } catch (e: Exception) {
            return onFailure(e)
        }

        try {
            if (jsonRpcHistory.setRequest(payload.id, topic, payload.method, requestJson)) {
                val encryptedRequest = chaChaPolyCodec.encrypt(topic, requestJson, envelopeType, participants)

                relay.publish(topic.value, encryptedRequest, params.toRelay()) { result ->
                    result.fold(
                        onSuccess = { onSuccess() },
                        onFailure = { error ->
                            logger.error("JsonRpcInteractor: Cannot send the request, error: $error")
                            onFailure(error)
                        }
                    )
                }
            }
        } catch (e: Exception) {
            logger.error("JsonRpcInteractor: Cannot send the request, exception: $e")
            return onFailure(e)
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
        try {
            checkConnectionWorking()
        } catch (e: NoRelayConnectionException) {
            return onFailure(e)
        }

        try {
            val responseJson = serializer.serialize(response) ?: return onFailure(IllegalStateException("JsonRpcInteractor: Unknown result params"))
            val encryptedResponse = chaChaPolyCodec.encrypt(topic, responseJson, envelopeType, participants)

            relay.publish(topic.value, encryptedResponse, params.toRelay()) { result ->
                result.fold(
                    onSuccess = {
                        jsonRpcHistory.updateRequestWithResponse(response.id, responseJson)
                        onSuccess()
                    },
                    onFailure = { error ->
                        logger.error("JsonRpcInteractor: Cannot send the response, error: $error")
                        onFailure(error)
                    }
                )
            }
        } catch (e: Exception) {
            logger.error("JsonRpcInteractor: Cannot send the response, exception: $e")
            return onFailure(e)
        }
    }

    override fun respondWithParams(
        request: WCRequest,
        clientParams: ClientParams,
        irnParams: IrnParams,
        envelopeType: EnvelopeType,
        participants: Participants?,
        onFailure: (Throwable) -> Unit,
        onSuccess: () -> Unit
    ) {
        val result = JsonRpcResponse.JsonRpcResult(id = request.id, result = clientParams)

        publishJsonRpcResponse(request.topic, irnParams, result, envelopeType = envelopeType, participants = participants,
            onFailure = { error -> onFailure(error) },
            onSuccess = { onSuccess() }
        )
    }

    override fun respondWithParams(
        requestId: Long,
        topic: Topic,
        clientParams: ClientParams,
        irnParams: IrnParams,
        envelopeType: EnvelopeType,
        participants: Participants?,
        onFailure: (Throwable) -> Unit,
        onSuccess: () -> Unit
    ) {
        val result = JsonRpcResponse.JsonRpcResult(id = requestId, result = clientParams)

        publishJsonRpcResponse(topic, irnParams, result, envelopeType = envelopeType, participants = participants,
            onFailure = { error -> onFailure(error) },
            onSuccess = { onSuccess() }
        )
    }

    // TODO: Can we replace this function with different parameters? Instead of request, just pass request id and request topic.
    override fun respondWithSuccess(
        request: WCRequest,
        irnParams: IrnParams,
        envelopeType: EnvelopeType,
        participants: Participants?,
    ) {
        val result = JsonRpcResponse.JsonRpcResult(id = request.id, result = true)

        try {
            publishJsonRpcResponse(request.topic, irnParams, result, envelopeType = envelopeType, participants = participants,
                onFailure = { error -> handleError("Cannot send the responseWithSuccess, error: ${error.stackTraceToString()}") })
        } catch (e: Exception) {
            handleError("publishFailure; ${e.stackTraceToString()}")
        }
    }

    // TODO: Can we replace this function with different parameters? Instead of request, just pass request id and request topic. And we never use WCRequest in the onSuccess callback so we can remove that as well
    override fun respondWithError(
        request: WCRequest,
        error: Error,
        irnParams: IrnParams,
        envelopeType: EnvelopeType,
        participants: Participants?,
        onSuccess: (WCRequest) -> Unit,
        onFailure: (Throwable) -> Unit,
    ) {
        logger.error("Responding with error: ${error.message}: ${error.code}")
        val jsonRpcError = JsonRpcResponse.JsonRpcError(id = request.id, error = JsonRpcResponse.Error(error.code, error.message))

        try {
            publishJsonRpcResponse(request.topic, irnParams, jsonRpcError, envelopeType = envelopeType, participants = participants,
                onSuccess = { onSuccess(request) },
                onFailure = { failure ->
                    onFailure(failure)
                    handleError("Cannot send respondWithError: ${failure.stackTraceToString()}")
                })
        } catch (e: Exception) {
            handleError("publishFailure; ${e.stackTraceToString()}")
        }
    }

    override fun respondWithError(
        requestId: Long,
        topic: Topic,
        error: Error,
        irnParams: IrnParams,
        envelopeType: EnvelopeType,
        participants: Participants?,
        onSuccess: () -> Unit,
        onFailure: (Throwable) -> Unit,
    ) {
        logger.error("Responding with error: ${error.message}: ${error.code}")
        val jsonRpcError = JsonRpcResponse.JsonRpcError(id = requestId, error = JsonRpcResponse.Error(error.code, error.message))

        try {
            publishJsonRpcResponse(topic, irnParams, jsonRpcError, envelopeType = envelopeType, participants = participants,
                onSuccess = { onSuccess() },
                onFailure = { failure ->
                    onFailure(failure)
                    handleError("Cannot send respondWithError: ${failure.stackTraceToString()}")
                })
        } catch (e: Exception) {
            handleError("publishFailure; ${e.stackTraceToString()}")
        }
    }

    override fun subscribe(topic: Topic, onSuccess: (Topic) -> Unit, onFailure: (Throwable) -> Unit) {
        try {
            checkConnectionWorking()
        } catch (e: NoRelayConnectionException) {
            return onFailure(e)
        }

        relay.subscribe(topic.value) { result ->
            result.fold(
                onSuccess = { acknowledgement ->
                    subscriptions[topic.value] = acknowledgement.result
                    onSuccess(topic)
                },
                onFailure = { error ->
                    logger.error("Subscribe to topic error: $topic error: $error")
                    onFailure(error)
                }
            )
        }
    }

    override fun batchSubscribe(topics: List<String>, onSuccess: (List<String>) -> Unit, onFailure: (Throwable) -> Unit) {
        try {
            checkConnectionWorking()
        } catch (e: NoRelayConnectionException) {
            return onFailure(e)
        }

        if (topics.isNotEmpty()) {
            relay.batchSubscribe(topics) { result ->
                result.fold(
                    onSuccess = { acknowledgement ->
                        subscriptions.plusAssign(topics.zip(acknowledgement.result).toMap())
                        onSuccess(topics)
                    },
                    onFailure = { error ->
                        logger.error("Batch subscribe to topics error: $topics error: $error")
                        onFailure(error)
                    }
                )
            }
        }
    }

    override fun unsubscribe(topic: Topic, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
        try {
            checkConnectionWorking()
        } catch (e: NoRelayConnectionException) {
            return onFailure(e)
        }

        if (subscriptions.contains(topic.value)) {
            val subscriptionId = SubscriptionId(subscriptions[topic.value].toString())
            relay.unsubscribe(topic.value, subscriptionId.id) { result ->
                result.fold(
                    onSuccess = {
                        scope.launch {
                            supervisorScope {
                                jsonRpcHistory.deleteRecordsByTopic(topic)
                                subscriptions.remove(topic.value)
                                pushMessageStorage.deletePushMessagesByTopic(topic.value)
                                onSuccess()
                            }
                        }
                    },
                    onFailure = { error ->
                        logger.error("Unsubscribe to topic: $topic error: $error")
                        onFailure(error)
                    }
                )
            }
        } else {
            onFailure(NoSuchElementException(Uncategorized.NoMatchingTopic("Session", topic.value).message))
        }
    }

    private fun manageSubscriptions() {
        scope.launch {
            relay.subscriptionRequest.map { relayRequest ->
                //TODO silences 4050
                if (relayRequest.tag == 4050) return@map Triple(String.Empty, Topic(""), 0L)
                val topic = Topic(relayRequest.subscriptionTopic)
                storePushRequestsIfEnabled(relayRequest, topic)
                Triple(decryptMessage(topic, relayRequest), topic, relayRequest.publishedAt)
            }.collect { (decryptedMessage, topic, publishedAt) ->
                if (decryptedMessage.isNotEmpty()) {
                    try {
                        manageSubscriptions(decryptedMessage, topic, publishedAt)
                    } catch (e: Exception) {
                        handleError("ManSub: ${e.stackTraceToString()}")
                    }
                }
            }
        }
    }

    private fun storePushRequestsIfEnabled(relayRequest: Relay.Model.Call.Subscription.Request, topic: Topic) {
        pushMessageStorage.arePushNotificationsEnabled
            .filter { areEnabled -> areEnabled }
            .onEach {
                pushMessageStorage.notificationTags
                    .filter { tag -> tag == relayRequest.tag }
                    .onEach { tag -> pushMessageStorage.insertPushMessage(sha256(relayRequest.message.toByteArray()), topic.value, relayRequest.message, tag) }
            }.launchIn(scope)
    }

    private fun decryptMessage(topic: Topic, relayRequest: Relay.Model.Call.Subscription.Request) =
        try {
            chaChaPolyCodec.decrypt(topic, relayRequest.message)
        } catch (e: Exception) {
            handleError("ManSub: ${e.stackTraceToString()}")
            String.Empty
        }

    private suspend fun manageSubscriptions(decryptedMessage: String, topic: Topic, publishedAt: Long) {
        serializer.tryDeserialize<ClientJsonRpc>(decryptedMessage)?.let { clientJsonRpc ->
            handleRequest(clientJsonRpc, topic, decryptedMessage, publishedAt)
        } ?: serializer.tryDeserialize<JsonRpcResponse.JsonRpcResult>(decryptedMessage)?.let { result ->
            handleJsonRpcResult(result, topic)
        } ?: serializer.tryDeserialize<JsonRpcResponse.JsonRpcError>(decryptedMessage)?.let { error ->
            handleJsonRpcError(error)
        } ?: handleError("JsonRpcInteractor: Received unknown object type")
    }

    private suspend fun handleRequest(clientJsonRpc: ClientJsonRpc, topic: Topic, decryptedMessage: String, publishedAt: Long) {
        if (jsonRpcHistory.setRequest(clientJsonRpc.id, topic, clientJsonRpc.method, decryptedMessage)) {
            serializer.deserialize(clientJsonRpc.method, decryptedMessage)?.let { params ->
                _clientSyncJsonRpc.emit(WCRequest(topic, clientJsonRpc.id, clientJsonRpc.method, params, decryptedMessage, publishedAt))
            } ?: handleError("JsonRpcInteractor: Unknown request params")
        }
    }

    private suspend fun handleJsonRpcResult(jsonRpcResult: JsonRpcResponse.JsonRpcResult, topic: Topic) {
        val serializedResult = serializer.serialize(jsonRpcResult) ?: return handleError("JsonRpcInteractor: Unknown result params")
        val jsonRpcRecord = jsonRpcHistory.updateRequestWithResponse(jsonRpcResult.id, serializedResult)

        if (jsonRpcRecord != null) {
            serializer.deserialize(jsonRpcRecord.method, jsonRpcRecord.body)?.let { params ->
                val responseVO = JsonRpcResponse.JsonRpcResult(jsonRpcResult.id, result = jsonRpcResult.result)
                _peerResponse.emit(jsonRpcRecord.toWCResponse(responseVO, params))
            } ?: handleError("JsonRpcInteractor: Unknown result params")
        } else {
            handleJsonRpcResponsesWithoutStoredRequest(jsonRpcResult, topic)
        }
    }

    private suspend fun handleJsonRpcResponsesWithoutStoredRequest(jsonRpcResult: JsonRpcResponse.JsonRpcResult, topic: Topic) {
        // todo: HANDLE DUPLICATES! maybe store results to check for duplicates????? https://github.com/WalletConnect/WalletConnectKotlinV2/issues/871
        //  Currently it's engine/usecase responsibility to handle duplicate responses
        if (jsonRpcResult.result is ChatNotifyResponseAuthParams.ResponseAuth) _peerResponse.emit(WCResponse(topic, String.Empty, jsonRpcResult, jsonRpcResult.result))
    }

    private suspend fun handleJsonRpcError(jsonRpcError: JsonRpcResponse.JsonRpcError) {
        val serializedResult = serializer.serialize(jsonRpcError) ?: return handleError("JsonRpcInteractor: Unknown result params")
        val jsonRpcRecord = jsonRpcHistory.updateRequestWithResponse(jsonRpcError.id, serializedResult)

        if (jsonRpcRecord != null) {
            serializer.deserialize(jsonRpcRecord.method, jsonRpcRecord.body)?.let { params ->
                _peerResponse.emit(jsonRpcRecord.toWCResponse(jsonRpcError, params))
            } ?: handleError("JsonRpcInteractor: Unknown error params")
        }
    }

    private fun handleError(errorMessage: String) {
        logger.error("JsonRpcInteractor error: $errorMessage")
        scope.launch {
            _internalErrors.emit(SDKError(Throwable(errorMessage)))
        }
    }
}