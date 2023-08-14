@file:JvmSynthetic

package com.walletconnect.auth.engine.domain

import com.walletconnect.android.Core
import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.exception.Invalid
import com.walletconnect.android.internal.common.exception.InvalidExpiryException
import com.walletconnect.android.internal.common.exception.Uncategorized
import com.walletconnect.android.internal.common.json_rpc.data.JsonRpcSerializer
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.AppMetaDataType
import com.walletconnect.android.internal.common.model.ConnectionState
import com.walletconnect.android.internal.common.model.EnvelopeType
import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.Participants
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.model.SymmetricKey
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.WCRequest
import com.walletconnect.android.internal.common.model.WCResponse
import com.walletconnect.android.internal.common.model.params.CoreAuthParams
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.signing.cacao.Cacao
import com.walletconnect.android.internal.common.signing.cacao.CacaoType
import com.walletconnect.android.internal.common.signing.cacao.CacaoVerifier
import com.walletconnect.android.internal.common.signing.cacao.Issuer
import com.walletconnect.android.internal.common.storage.VerifyContextStorageRepository
import com.walletconnect.android.internal.utils.CoreValidator
import com.walletconnect.android.internal.utils.DAY_IN_SECONDS
import com.walletconnect.android.internal.utils.MONTH_IN_SECONDS
import com.walletconnect.android.internal.utils.getParticipantTag
import com.walletconnect.android.pairing.client.PairingInterface
import com.walletconnect.android.pairing.handler.PairingControllerInterface
import com.walletconnect.android.pairing.model.mapper.toClient
import com.walletconnect.android.verify.data.model.VerifyContext
import com.walletconnect.android.verify.domain.ResolveAttestationIdUseCase
import com.walletconnect.auth.client.mapper.toCommon
import com.walletconnect.auth.common.exceptions.InvalidCacaoException
import com.walletconnect.auth.common.exceptions.InvalidParamsException
import com.walletconnect.auth.common.exceptions.MissingAuthRequestException
import com.walletconnect.auth.common.exceptions.PeerError
import com.walletconnect.auth.common.json_rpc.AuthParams
import com.walletconnect.auth.common.json_rpc.AuthRpc
import com.walletconnect.auth.common.model.AuthResponse
import com.walletconnect.auth.common.model.Events
import com.walletconnect.auth.common.model.PayloadParams
import com.walletconnect.auth.common.model.PendingRequest
import com.walletconnect.auth.common.model.Requester
import com.walletconnect.auth.common.model.Respond
import com.walletconnect.auth.engine.mapper.toCAIP122Message
import com.walletconnect.auth.engine.mapper.toCacaoPayload
import com.walletconnect.auth.engine.mapper.toPendingRequest
import com.walletconnect.auth.engine.pairingTopicToResponseTopicMap
import com.walletconnect.auth.json_rpc.domain.GetPendingJsonRpcHistoryEntriesUseCase
import com.walletconnect.auth.json_rpc.domain.GetPendingJsonRpcHistoryEntryByIdUseCase
import com.walletconnect.auth.json_rpc.model.JsonRpcMethod
import com.walletconnect.auth.json_rpc.model.JsonRpcMethod.WC_AUTH_REQUEST
import com.walletconnect.auth.use_case.FormatMessageUseCase
import com.walletconnect.auth.use_case.FormatMessageUseCaseInterface
import com.walletconnect.auth.use_case.RespondAuthRequestUseCase
import com.walletconnect.auth.use_case.RespondAuthRequestUseCaseInterface
import com.walletconnect.auth.use_case.SendAuthRequestUseCase
import com.walletconnect.auth.use_case.SendAuthRequestUseCaseInterface
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.foundation.util.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withTimeout
import java.util.Date
import java.util.concurrent.TimeUnit

internal class AuthEngine(
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val getPendingJsonRpcHistoryEntriesUseCase: GetPendingJsonRpcHistoryEntriesUseCase,
    private val getPendingJsonRpcHistoryEntryByIdUseCase: GetPendingJsonRpcHistoryEntryByIdUseCase,
    private val crypto: KeyManagementRepository,
    private val pairingHandler: PairingControllerInterface,
    private val pairingInterface: PairingInterface,
    private val serializer: JsonRpcSerializer,
    private val resolveAttestationIdUseCase: ResolveAttestationIdUseCase,
    private val verifyContextStorageRepository: VerifyContextStorageRepository,
    private val selfAppMetaData: AppMetaData,
    private val cacaoVerifier: CacaoVerifier,
    private val sendAuthRequestUseCase: SendAuthRequestUseCase,
    private val respondAuthRequestUseCase: RespondAuthRequestUseCase,
    private val formatMessageUseCase: FormatMessageUseCase,
    private val logger: Logger,
) : SendAuthRequestUseCaseInterface by sendAuthRequestUseCase,
    RespondAuthRequestUseCaseInterface by respondAuthRequestUseCase,
    FormatMessageUseCaseInterface by formatMessageUseCase {
    private var jsonRpcRequestsJob: Job? = null
    private var jsonRpcResponsesJob: Job? = null
    private var internalErrorsJob: Job? = null
    private var authEventsJob: Job? = null

    private val _engineEvent: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val engineEvent: SharedFlow<EngineEvent> = _engineEvent.asSharedFlow()

    init {
        pairingHandler.register(JsonRpcMethod.WC_AUTH_REQUEST)
    }

    fun setup() {
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
                if (authEventsJob == null) {
                    authEventsJob = collectInternalErrors()
                }
            }
            .launchIn(scope)
    }

    internal fun getPendingRequests(): List<PendingRequest> {
        return getPendingJsonRpcHistoryEntriesUseCase()
            .map { jsonRpcHistoryEntry -> jsonRpcHistoryEntry.toPendingRequest() }
    }

    internal suspend fun getVerifyContext(id: Long): VerifyContext? = verifyContextStorageRepository.get(id)
    internal suspend fun getListOfVerifyContext(): List<VerifyContext> = verifyContextStorageRepository.getAll()

    private fun onAuthRequest(wcRequest: WCRequest, authParams: AuthParams.RequestParams) {
        val irnParams = IrnParams(Tags.AUTH_REQUEST_RESPONSE, Ttl(DAY_IN_SECONDS))
        try {
            if (!CoreValidator.isExpiryWithinBounds(authParams.expiry)) {
                jsonRpcInteractor.respondWithError(wcRequest, Invalid.RequestExpired, irnParams)
                return
            }

            val json = serializer.serialize(AuthRpc.AuthRequest(id = wcRequest.id, params = authParams)) ?: throw Exception("Error serializing session proposal")
            val url = authParams.requester.metadata.url
            resolveAttestationIdUseCase(wcRequest.id, json, url) { verifyContext ->
                scope.launch {
                    _engineEvent.emit(Events.OnAuthRequest(wcRequest.id, wcRequest.topic.value, authParams.payloadParams, verifyContext))
                }
            }
        } catch (e: Exception) {
            jsonRpcInteractor.respondWithError(
                wcRequest,
                Uncategorized.GenericError("Cannot handle a auth request: ${e.message}, topic: ${wcRequest.topic}"),
                irnParams
            )
            scope.launch { _engineEvent.emit(SDKError(e)) }
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
                    val (header, payload, signature) = (response.result as CoreAuthParams.ResponseParams)
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
            scope.launch { _engineEvent.emit(SDKError(e)) }
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
            .filter { response -> response.params is AuthParams }
            .onEach { response -> onAuthRequestResponse(response, response.params as AuthParams.RequestParams) }
            .launchIn(scope)

    private fun resubscribeToPendingRequestsTopics() {
        val responseTopics = pairingTopicToResponseTopicMap.map { (_, responseTopic) -> responseTopic.value }
        try {
            jsonRpcInteractor.batchSubscribe(responseTopics) { error -> scope.launch { _engineEvent.emit(SDKError(error)) } }
        } catch (e: Exception) {
            scope.launch { _engineEvent.emit(SDKError(e)) }
        }
    }

    private fun collectAuthEvents(): Job =
        merge(sendAuthRequestUseCase.events)
            .onEach { event -> _engineEvent.emit(event) }
            .launchIn(scope)

    private fun collectInternalErrors(): Job =
        merge(jsonRpcInteractor.internalErrors, pairingHandler.findWrongMethodsFlow)
            .onEach { exception -> _engineEvent.emit(exception) }
            .launchIn(scope)
}