package com.walletconnect.walletconnectv2.relay.domain

import com.tinder.scarlet.WebSocket
import com.walletconnect.walletconnectv2.core.exceptions.client.WalletConnectException
import com.walletconnect.walletconnectv2.core.exceptions.peer.PeerError
import com.walletconnect.walletconnectv2.core.model.type.SettlementSequence
import com.walletconnect.walletconnectv2.core.model.utils.JsonRpcMethod
import com.walletconnect.walletconnectv2.core.model.vo.*
import com.walletconnect.walletconnectv2.core.scope.scope
import com.walletconnect.walletconnectv2.network.NetworkRepository
import com.walletconnect.walletconnectv2.relay.data.serializer.JsonRpcSerializer
import com.walletconnect.walletconnectv2.relay.model.RelayDO
import com.walletconnect.walletconnectv2.relay.model.mapper.toJsonRpcErrorVO
import com.walletconnect.walletconnectv2.relay.model.mapper.toJsonRpcResultVO
import com.walletconnect.walletconnectv2.relay.model.mapper.toRelayDOJsonRpcResponse
import com.walletconnect.walletconnectv2.storage.history.JsonRpcHistory
import com.walletconnect.walletconnectv2.storage.history.model.JsonRpcStatus
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
    private val _clientSyncJsonRpc: MutableSharedFlow<RequestSubscriptionPayloadVO> = MutableSharedFlow()
    internal val clientSyncJsonRpc: SharedFlow<RequestSubscriptionPayloadVO> = _clientSyncJsonRpc

    private val _peerResponse: MutableSharedFlow<JsonRpcResponseVO> = MutableSharedFlow()
    val peerResponse: SharedFlow<JsonRpcResponseVO> = _peerResponse

    private val subscriptions: MutableMap<String, String> = mutableMapOf()

    private val _isConnectionOpened = MutableStateFlow(false)
    val isConnectionOpened: StateFlow<Boolean> = _isConnectionOpened

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

    internal fun publishJsonRpcRequests(topic: TopicVO, payload: SettlementSequence<*>, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
        val serializedPayload = serializer.serialize(payload, topic)

        if (jsonRpcHistory.setRequest(payload.id, topic, payload.method, serializedPayload)) {
            networkRepository.publish(topic, serializedPayload) { result ->
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
        val responseToDO = response.toRelayDOJsonRpcResponse()
        val serializedPayload = serializer.serialize(responseToDO, topic)

        networkRepository.publish(topic, serializedPayload) { result ->
            result.fold(
                onSuccess = {
                    jsonRpcHistory.updateRequestStatus(response.id, JsonRpcStatus.RESPOND_SUCCESS)
                    onSuccess()
                },
                onFailure = { error ->
                    jsonRpcHistory.updateRequestStatus(response.id, JsonRpcStatus.RESPOND_FAILURE)
                    onFailure(error)
                }
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

    internal fun getJsonRpcHistory(topic: TopicVO): Pair<List<JsonRpcHistoryVO>, List<JsonRpcHistoryVO>> {
        return jsonRpcHistory.getRequests(topic, listOfMethodsForRequests) to jsonRpcHistory.getResponses(topic, listOfMethodsForRequests)
    }

    internal fun respondWithError(request: WCRequestVO, error: PeerError) {
        Logger.error("Responding with error: ${error.message}: ${error.code}")
        val jsonRpcError = JsonRpcResponseVO.JsonRpcError(id = request.id, error = JsonRpcResponseVO.Error(error.code, error.message))
        publishJsonRpcResponse(request.topic, jsonRpcError, onFailure = { failure -> Logger.error("Cannot respond with error: $failure") })
    }

    internal fun respondWithSuccess(request: WCRequestVO) {
        val jsonRpcResult = JsonRpcResponseVO.JsonRpcResult(id = request.id, result = "true")
        publishJsonRpcResponse(request.topic, jsonRpcResult, onFailure = { error -> Logger.error("Cannot send the response, error: $error") })
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

                    Logger.error("Kobe; decoded peer message: $decryptedMessage")
                    handleSessionRequest(decryptedMessage, topic)
                    handleJsonRpcResponse(decryptedMessage)
                }
        }
    }

    private suspend fun handleSessionRequest(decryptedMessage: String, topic: TopicVO) {
        val clientJsonRpc = serializer.tryDeserialize<RelayDO.ClientJsonRpc>(decryptedMessage)
        if (clientJsonRpc != null && jsonRpcHistory.setRequest(clientJsonRpc.id, topic, clientJsonRpc.method, decryptedMessage)) {
            serializer.deserialize(clientJsonRpc.method, decryptedMessage)?.let { params ->
                _clientSyncJsonRpc.emit(RequestSubscriptionPayloadVO(params, WCRequestVO(topic, clientJsonRpc.id, clientJsonRpc.method)))
            } ?: Logger.error("Deserialization error: $clientJsonRpc")
        }
    }

    private suspend fun handleJsonRpcResponse(decryptedMessage: String) {
        val acknowledgement = serializer.tryDeserialize<RelayDO.JsonRpcResponse.JsonRpcResult>(decryptedMessage)
        if (acknowledgement != null) {
            jsonRpcHistory.updateRequestStatus(acknowledgement.id, JsonRpcStatus.REQUEST_SUCCESS)
            _peerResponse.emit(acknowledgement.toJsonRpcResultVO())
        }

        val error = serializer.tryDeserialize<RelayDO.JsonRpcResponse.JsonRpcError>(decryptedMessage)
        if (error != null) {
            jsonRpcHistory.updateRequestStatus(error.id, JsonRpcStatus.REQUEST_FAILURE)
            _peerResponse.emit(error.toJsonRpcErrorVO())
        }
    }

    private fun setOnConnectionOpen(event: WebSocket.Event) {
        if (event is WebSocket.Event.OnConnectionOpened<*>) {
            _isConnectionOpened.compareAndSet(expect = false, update = true)
        } else if (event is WebSocket.Event.OnConnectionClosed) {
            _isConnectionOpened.compareAndSet(expect = true, update = false)
        }
    }

    private companion object {
        val listOfMethodsForRequests = listOf(
            JsonRpcMethod.WC_PAIRING_APPROVE,
            JsonRpcMethod.WC_PAIRING_UPDATE,
            JsonRpcMethod.WC_PAIRING_PING,
            JsonRpcMethod.WC_SESSION_PROPOSE,
            JsonRpcMethod.WC_SESSION_APPROVE,
            JsonRpcMethod.WC_SESSION_REJECT,
            JsonRpcMethod.WC_SESSION_DELETE,
            JsonRpcMethod.WC_SESSION_PAYLOAD,
            JsonRpcMethod.WC_SESSION_UPDATE,
            JsonRpcMethod.WC_SESSION_UPGRADE,
            JsonRpcMethod.WC_SESSION_NOTIFICATION,
            JsonRpcMethod.WC_SESSION_PING,
        )
    }
}