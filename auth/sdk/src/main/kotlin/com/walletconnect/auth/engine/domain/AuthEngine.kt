@file:JvmSynthetic

package com.walletconnect.auth.engine.domain

import com.walletconnect.android.Core
import com.walletconnect.android.impl.common.SDKError
import com.walletconnect.android.impl.common.model.ConnectionState
import com.walletconnect.android.impl.common.model.type.EngineEvent
import com.walletconnect.android.impl.utils.DAY_IN_SECONDS
import com.walletconnect.android.impl.utils.Logger
import com.walletconnect.android.impl.utils.MONTH_IN_SECONDS
import com.walletconnect.android.impl.utils.SELF_PARTICIPANT_CONTEXT
import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.crypto.KeyManagementRepository
import com.walletconnect.android.internal.common.exception.InvalidProjectIdException
import com.walletconnect.android.internal.common.exception.ProjectIdDoesNotExistException
import com.walletconnect.android.internal.common.model.*
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.pairing.client.PairingInterface
import com.walletconnect.android.pairing.handler.PairingControllerInterface
import com.walletconnect.android.pairing.model.mapper.toClient
import com.walletconnect.auth.client.mapper.toCommon
import com.walletconnect.auth.common.exceptions.InvalidCacaoException
import com.walletconnect.auth.common.exceptions.MissingAuthRequestException
import com.walletconnect.auth.common.exceptions.MissingIssuerException
import com.walletconnect.auth.common.exceptions.PeerError
import com.walletconnect.auth.common.json_rpc.AuthParams
import com.walletconnect.auth.common.json_rpc.AuthRpc
import com.walletconnect.auth.common.model.*
import com.walletconnect.auth.engine.mapper.toCAIP122Message
import com.walletconnect.auth.engine.mapper.toCacaoPayload
import com.walletconnect.auth.engine.mapper.toPendingRequest
import com.walletconnect.auth.json_rpc.domain.GetPendingJsonRpcHistoryEntriesUseCase
import com.walletconnect.auth.json_rpc.domain.GetPendingJsonRpcHistoryEntryByIdUseCase
import com.walletconnect.auth.json_rpc.model.JsonRpcMethod
import com.walletconnect.auth.signature.CacaoType
import com.walletconnect.auth.signature.cacao.CacaoVerifier
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.util.generateId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

internal class AuthEngine(
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val getPendingJsonRpcHistoryEntriesUseCase: GetPendingJsonRpcHistoryEntriesUseCase,
    private val getPendingJsonRpcHistoryEntryByIdUseCase: GetPendingJsonRpcHistoryEntryByIdUseCase,
    private val crypto: KeyManagementRepository,
    private val pairingHandler: PairingControllerInterface,
    private val pairingInterface: PairingInterface,
    private val selfAppMetaData: AppMetaData,
    private val issuer: Issuer?,
    private val cacaoVerifier: CacaoVerifier
) {
    private var jsonRpcRequestsJob: Job? = null
    private var jsonRpcResponsesJob: Job? = null
    private var internalErrorsJob: Job? = null
    private val _engineEvent: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val engineEvent: SharedFlow<EngineEvent> = _engineEvent.asSharedFlow()

    // idea: If we need responseTopic persistence throughout app terminations this is not sufficient. Decide after Alpha
    private val pairingTopicToResponseTopicMap: MutableMap<Topic, Topic> = mutableMapOf()

    init {
        pairingHandler.register(JsonRpcMethod.WC_AUTH_REQUEST)
    }

    fun setup() {
        jsonRpcInteractor.wsConnectionFailedFlow.onEach { walletConnectException ->
            when (walletConnectException) {
                is ProjectIdDoesNotExistException, is InvalidProjectIdException -> _engineEvent.emit(ConnectionState(false, walletConnectException))
                else -> _engineEvent.emit(SDKError(InternalError(walletConnectException)))
            }
        }.launchIn(scope)

        jsonRpcInteractor.isConnectionAvailable
            .onEach { isAvailable -> _engineEvent.emit(ConnectionState(isAvailable)) }
            .filter { isAvailable: Boolean -> isAvailable }
            .onEach {
                supervisorScope {
                    launch(Dispatchers.IO) { resubscribeToPendingRequestsTopics() }
                }

                if (jsonRpcRequestsJob == null) {
                    jsonRpcRequestsJob = collectJsonRpcRequests()
                }
                if (jsonRpcResponsesJob == null) {
                    jsonRpcResponsesJob = collectJsonRpcResponses()
                }
                if (internalErrorsJob == null) {
                    internalErrorsJob = collectInternalErrors()
                }
            }
            .launchIn(scope)
    }

    internal fun request(
        payloadParams: PayloadParams,
        pairing: Pairing,
        onSuccess: () -> Unit,
        onFailure: (Throwable) -> Unit,
    ) {
        val responsePublicKey: PublicKey = crypto.generateKeyPair()
        val responseTopic: Topic = crypto.getTopicFromKey(responsePublicKey)
        val authParams: AuthParams.RequestParams = AuthParams.RequestParams(Requester(responsePublicKey.keyAsHex, selfAppMetaData), payloadParams)
        val authRequest: AuthRpc.AuthRequest = AuthRpc.AuthRequest(generateId(), params = authParams)
        val irnParams = IrnParams(Tags.AUTH_REQUEST, Ttl(DAY_IN_SECONDS), true)
        val pairingTopic = pairing.topic
        crypto.setKey(responsePublicKey, "${SELF_PARTICIPANT_CONTEXT}${responseTopic.value}")

        jsonRpcInteractor.publishJsonRpcRequest(pairingTopic, irnParams, authRequest,
            onSuccess = {
                try {
                    jsonRpcInteractor.subscribe(responseTopic) { error ->
                        return@subscribe onFailure(error)
                    }
                } catch (e: Exception) {
                    return@publishJsonRpcRequest onFailure(e)
                }

                pairingTopicToResponseTopicMap[pairingTopic] = responseTopic
                onSuccess()
            },
            onFailure = { error ->
                Logger.error("Failed to send a auth request: $error")
                onFailure(error)
            }
        )
    }

    internal fun respond(
        respond: Respond,
        onFailure: (Throwable) -> Unit,
    ) {
        val jsonRpcHistoryEntry = getPendingJsonRpcHistoryEntryByIdUseCase(respond.id)

        if (jsonRpcHistoryEntry == null) {
            Logger.error(MissingAuthRequestException.message)
            onFailure(MissingAuthRequestException)
            return
        }

        val authParams: AuthParams.RequestParams = jsonRpcHistoryEntry.params
        val response: JsonRpcResponse = when (respond) {
            is Respond.Error -> JsonRpcResponse.JsonRpcError(respond.id, error = JsonRpcResponse.Error(respond.code, respond.message))
            is Respond.Result -> {
                val issuer: Issuer = issuer ?: throw MissingIssuerException
                val payload: Cacao.Payload = authParams.payloadParams.toCacaoPayload(issuer)
                val cacao = Cacao(CacaoType.EIP4361.toHeader(), payload, respond.signature.toCommon())
                val responseParams = AuthParams.ResponseParams(cacao.header, cacao.payload, cacao.signature)

                if (!cacaoVerifier.verify(cacao)) throw InvalidCacaoException
                JsonRpcResponse.JsonRpcResult(respond.id, result = responseParams)
            }
        }

        val receiverPublicKey = PublicKey(authParams.requester.publicKey)
        val senderPublicKey: PublicKey = crypto.generateKeyPair()
        val symmetricKey: SymmetricKey = crypto.generateSymmetricKeyFromKeyAgreement(senderPublicKey, receiverPublicKey)
        val responseTopic: Topic = crypto.getTopicFromKey(receiverPublicKey)
        crypto.setKey(symmetricKey, responseTopic.value)

        val irnParams = IrnParams(Tags.AUTH_REQUEST_RESPONSE, Ttl(DAY_IN_SECONDS), false)
        jsonRpcInteractor.publishJsonRpcResponse(
            responseTopic, irnParams, response, envelopeType = EnvelopeType.ONE, participants = Participants(senderPublicKey, receiverPublicKey),
            onSuccess = { Logger.log("Success Responded on topic: $responseTopic") },
            onFailure = { Logger.error("Error Responded on topic: $responseTopic") }
        )
    }

    internal fun getPendingRequests(): List<PendingRequest> {
        if (issuer == null) {
            throw MissingIssuerException
        }
        return getPendingJsonRpcHistoryEntriesUseCase()
            .map { jsonRpcHistoryEntry -> jsonRpcHistoryEntry.toPendingRequest(issuer) }
    }

    private fun onAuthRequest(wcRequest: WCRequest, authParams: AuthParams.RequestParams) {
        if (issuer != null) {
            scope.launch {
                val formattedMessage: String = authParams.payloadParams.toCAIP122Message(issuer)
                _engineEvent.emit(Events.OnAuthRequest(wcRequest.id, formattedMessage))
            }
        } else {
            val irnParams = IrnParams(Tags.AUTH_REQUEST_RESPONSE, Ttl(DAY_IN_SECONDS), false)
            jsonRpcInteractor.respondWithError(wcRequest, PeerError.MissingIssuer, irnParams)
        }
    }

    private fun onAuthRequestResponse(wcResponse: WCResponse, requestParams: AuthParams.RequestParams) {
        try {
            val pairingTopic = wcResponse.topic
            updatePairing(pairingTopic, requestParams)
            if (!pairingInterface.getPairings().any { pairing -> pairing.topic == pairingTopic.value }) return
            pairingTopicToResponseTopicMap.remove(pairingTopic)

            when (val response = wcResponse.response) {
                is JsonRpcResponse.JsonRpcError -> {
                    scope.launch {
                        _engineEvent.emit(Events.OnAuthResponse(response.id, AuthResponse.Error(response.error.code, response.error.message)))
                    }
                }
                is JsonRpcResponse.JsonRpcResult -> {
                    val (header, payload, signature) = (response.result as AuthParams.ResponseParams)
                    val cacao = Cacao(header, payload, signature)
                    if (cacaoVerifier.verify(cacao)) {
                        scope.launch {
                            _engineEvent.emit(Events.OnAuthResponse(response.id, AuthResponse.Result(cacao)))
                        }
                    } else {
                        scope.launch {
                            _engineEvent.emit(
                                Events.OnAuthResponse(
                                    response.id,
                                    AuthResponse.Error(PeerError.SignatureVerificationFailed.code, PeerError.SignatureVerificationFailed.message)
                                )
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            scope.launch { _engineEvent.emit(SDKError(InternalError(e))) }
        }
    }

    private fun updatePairing(
        topic: Topic,
        requestParams: AuthParams.RequestParams
    ) {
        pairingHandler.updateExpiry(Core.Params.UpdateExpiry(topic.value, Expiry(MONTH_IN_SECONDS)))
        pairingHandler.updateMetadata(Core.Params.UpdateMetadata(topic.value, requestParams.requester.metadata.toClient(), AppMetaDataType.PEER))
        pairingHandler.activate(Core.Params.Activate(topic.value))
    }

    private fun collectJsonRpcRequests(): Job =
        jsonRpcInteractor.clientSyncJsonRpc
            .filter { request -> request.params is AuthParams.RequestParams }
            .onEach { request -> onAuthRequest(request, request.params as AuthParams.RequestParams) }
            .launchIn(scope)

    private fun collectJsonRpcResponses(): Job =
        jsonRpcInteractor.peerResponse
            .filter { response -> response.params is AuthParams.RequestParams }
            .onEach { response -> onAuthRequestResponse(response, response.params as AuthParams.RequestParams) }
            .launchIn(scope)

    private fun resubscribeToPendingRequestsTopics() {
        pairingTopicToResponseTopicMap
            .map { it.value }
            .onEach { responseTopic: Topic ->
                try {
                    jsonRpcInteractor.subscribe(responseTopic) { error ->
                        scope.launch {
                            _engineEvent.emit(SDKError(InternalError(error)))
                        }
                    }
                } catch (e: Exception) {
                    scope.launch {
                        _engineEvent.emit(SDKError(InternalError(e)))
                    }
                }
            }
    }

    private fun collectInternalErrors(): Job =
        merge(jsonRpcInteractor.internalErrors, pairingHandler.findWrongMethodsFlow)
            .onEach { exception -> _engineEvent.emit(SDKError(exception)) }
            .launchIn(scope)
}