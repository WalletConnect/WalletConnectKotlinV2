@file:JvmSynthetic

package com.walletconnect.sign.engine.domain

import com.walletconnect.android.Core
import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.exception.CannotFindSequenceForTopic
import com.walletconnect.android.internal.common.exception.Invalid
import com.walletconnect.android.internal.common.exception.Reason
import com.walletconnect.android.internal.common.exception.Uncategorized
import com.walletconnect.android.internal.common.json_rpc.data.JsonRpcSerializer
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.AppMetaDataType
import com.walletconnect.android.internal.common.model.ConnectionState
import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.Validation
import com.walletconnect.android.internal.common.model.WCRequest
import com.walletconnect.android.internal.common.model.WCResponse
import com.walletconnect.android.internal.common.model.params.CoreSignParams
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.storage.MetadataStorageRepositoryInterface
import com.walletconnect.android.internal.common.storage.VerifyContextStorageRepository
import com.walletconnect.android.internal.utils.CoreValidator
import com.walletconnect.android.internal.utils.DAY_IN_SECONDS
import com.walletconnect.android.internal.utils.FIVE_MINUTES_IN_SECONDS
import com.walletconnect.android.internal.utils.MONTH_IN_SECONDS
import com.walletconnect.android.internal.utils.THIRTY_SECONDS
import com.walletconnect.android.internal.utils.WEEK_IN_SECONDS
import com.walletconnect.android.pairing.client.PairingInterface
import com.walletconnect.android.pairing.handler.PairingControllerInterface
import com.walletconnect.android.pairing.model.mapper.toClient
import com.walletconnect.android.pairing.model.mapper.toPairing
import com.walletconnect.android.verify.data.model.VerifyContext
import com.walletconnect.android.verify.domain.ResolveAttestationIdUseCase
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.foundation.util.Logger
import com.walletconnect.sign.common.exceptions.InvalidEventException
import com.walletconnect.sign.common.exceptions.NO_SEQUENCE_FOR_TOPIC_MESSAGE
import com.walletconnect.sign.common.exceptions.NotSettledSessionException
import com.walletconnect.sign.common.exceptions.PeerError
import com.walletconnect.sign.common.exceptions.SESSION_IS_NOT_ACKNOWLEDGED_MESSAGE
import com.walletconnect.sign.common.exceptions.UNAUTHORIZED_EMIT_MESSAGE
import com.walletconnect.sign.common.exceptions.UNAUTHORIZED_EXTEND_MESSAGE
import com.walletconnect.sign.common.exceptions.UnauthorizedEventException
import com.walletconnect.sign.common.exceptions.UnauthorizedPeerException
import com.walletconnect.sign.common.model.PendingRequest
import com.walletconnect.sign.common.model.type.Sequences
import com.walletconnect.sign.common.model.vo.clientsync.common.NamespaceVO
import com.walletconnect.sign.common.model.vo.clientsync.session.SignRpc
import com.walletconnect.sign.common.model.vo.clientsync.session.params.SignParams
import com.walletconnect.sign.common.model.vo.clientsync.session.payload.SessionEventVO
import com.walletconnect.sign.common.model.vo.proposal.ProposalVO
import com.walletconnect.sign.common.model.vo.sequence.SessionVO
import com.walletconnect.sign.common.validator.SignValidator
import com.walletconnect.sign.engine.model.EngineDO
import com.walletconnect.sign.engine.model.mapper.toEngineDO
import com.walletconnect.sign.engine.model.mapper.toEngineDOEvent
import com.walletconnect.sign.engine.model.mapper.toEngineDOSessionExtend
import com.walletconnect.sign.engine.model.mapper.toMapOfEngineNamespacesSession
import com.walletconnect.sign.engine.model.mapper.toPeerError
import com.walletconnect.sign.engine.model.mapper.toSessionApproved
import com.walletconnect.sign.engine.model.mapper.toSessionRequest
import com.walletconnect.sign.engine.model.mapper.toVO
import com.walletconnect.sign.engine.sessionRequestsQueue
import com.walletconnect.sign.engine.use_case.ApproveSessionUseCase
import com.walletconnect.sign.engine.use_case.ApproveSessionUseCaseInterface
import com.walletconnect.sign.engine.use_case.DisconnectSessionUseCase
import com.walletconnect.sign.engine.use_case.DisconnectSessionUseCaseInterface
import com.walletconnect.sign.engine.use_case.EmitEventUseCase
import com.walletconnect.sign.engine.use_case.EmitEventUseCaseInterface
import com.walletconnect.sign.engine.use_case.ExtendSessionUsesCase
import com.walletconnect.sign.engine.use_case.ExtendSessionUsesCaseInterface
import com.walletconnect.sign.engine.use_case.GetPairingsUseCase
import com.walletconnect.sign.engine.use_case.GetPairingsUseCaseInterface
import com.walletconnect.sign.engine.use_case.GetSessionProposalsUseCase
import com.walletconnect.sign.engine.use_case.GetSessionProposalsUseCaseInterface
import com.walletconnect.sign.engine.use_case.GetSessionsUseCase
import com.walletconnect.sign.engine.use_case.GetSessionsUseCaseInterface
import com.walletconnect.sign.engine.use_case.PairUseCase
import com.walletconnect.sign.engine.use_case.PairUseCaseInterface
import com.walletconnect.sign.engine.use_case.PingUseCase
import com.walletconnect.sign.engine.use_case.PingUseCaseInterface
import com.walletconnect.sign.engine.use_case.ProposeSessionUseCase
import com.walletconnect.sign.engine.use_case.ProposeSessionUseCaseInterface
import com.walletconnect.sign.engine.use_case.RejectSessionUseCase
import com.walletconnect.sign.engine.use_case.RejectSessionUseCaseInterface
import com.walletconnect.sign.engine.use_case.RespondSessionRequestUseCase
import com.walletconnect.sign.engine.use_case.RespondSessionRequestUseCaseInterface
import com.walletconnect.sign.engine.use_case.SessionRequestUseCase
import com.walletconnect.sign.engine.use_case.SessionRequestUseCaseInterface
import com.walletconnect.sign.engine.use_case.SessionUpdateUseCase
import com.walletconnect.sign.engine.use_case.SessionUpdateUseCaseInterface
import com.walletconnect.sign.json_rpc.domain.GetPendingRequestsUseCaseByTopic
import com.walletconnect.sign.json_rpc.domain.GetPendingRequestsUseCaseByTopicInterface
import com.walletconnect.sign.json_rpc.domain.GetPendingSessionRequests
import com.walletconnect.sign.json_rpc.model.JsonRpcMethod
import com.walletconnect.sign.storage.proposal.ProposalStorageRepository
import com.walletconnect.sign.storage.sequence.SessionStorageRepository
import com.walletconnect.utils.Empty
import com.walletconnect.utils.extractTimestamp
import com.walletconnect.utils.isSequenceValid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

internal class SignEngine(
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val getPendingRequestsByTopicUseCase: GetPendingRequestsUseCaseByTopic,
    private val getPendingSessionRequests: GetPendingSessionRequests,
    private val crypto: KeyManagementRepository,
    private val sessionStorageRepository: SessionStorageRepository,
    private val proposalStorageRepository: ProposalStorageRepository,
    private val metadataStorageRepository: MetadataStorageRepositoryInterface,
    private val pairingInterface: PairingInterface,
    private val pairingController: PairingControllerInterface,
    private val serializer: JsonRpcSerializer,
    private val resolveAttestationIdUseCase: ResolveAttestationIdUseCase,
    private val verifyContextStorageRepository: VerifyContextStorageRepository,
    private val selfAppMetaData: AppMetaData,
    private val proposeSessionUseCase: ProposeSessionUseCase,
    private val pairUseCase: PairUseCase,
    private val rejectSessionUseCase: RejectSessionUseCase,
    private val approveSessionUseCase: ApproveSessionUseCase,
    private val sessionUpdateUseCase: SessionUpdateUseCase,
    private val sessionRequestUseCase: SessionRequestUseCase,
    private val respondSessionRequestUseCase: RespondSessionRequestUseCase,
    private val pingUseCase: PingUseCase,
    private val emitEventUseCase: EmitEventUseCase,
    private val extendSessionUsesCase: ExtendSessionUsesCase,
    private val disconnectSessionUseCase: DisconnectSessionUseCase,
    private val getSessionsUseCase: GetSessionsUseCase,
    private val getPairingsUseCase: GetPairingsUseCase,
    private val getSessionProposalsUseCase: GetSessionProposalsUseCase,
    private val logger: Logger
) : ProposeSessionUseCaseInterface by proposeSessionUseCase,
    PairUseCaseInterface by pairUseCase,
    RejectSessionUseCaseInterface by rejectSessionUseCase,
    ApproveSessionUseCaseInterface by approveSessionUseCase,
    SessionUpdateUseCaseInterface by sessionUpdateUseCase,
    SessionRequestUseCaseInterface by sessionRequestUseCase,
    RespondSessionRequestUseCaseInterface by respondSessionRequestUseCase,
    PingUseCaseInterface by pingUseCase,
    EmitEventUseCaseInterface by emitEventUseCase,
    ExtendSessionUsesCaseInterface by extendSessionUsesCase,
    DisconnectSessionUseCaseInterface by disconnectSessionUseCase,
    GetSessionsUseCaseInterface by getSessionsUseCase,
    GetPairingsUseCaseInterface by getPairingsUseCase,
    GetPendingRequestsUseCaseByTopicInterface by getPendingRequestsByTopicUseCase,
    GetSessionProposalsUseCaseInterface by getSessionProposalsUseCase
{
    private var jsonRpcRequestsJob: Job? = null
    private var jsonRpcResponsesJob: Job? = null
    private var internalErrorsJob: Job? = null
    private var signEventsJob: Job? = null
    private val _engineEvent: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val engineEvent: SharedFlow<EngineEvent> = _engineEvent.asSharedFlow()

    init {
        pairingController.register(
            JsonRpcMethod.WC_SESSION_PROPOSE,
            JsonRpcMethod.WC_SESSION_SETTLE,
            JsonRpcMethod.WC_SESSION_REQUEST,
            JsonRpcMethod.WC_SESSION_EVENT,
            JsonRpcMethod.WC_SESSION_DELETE,
            JsonRpcMethod.WC_SESSION_EXTEND,
            JsonRpcMethod.WC_SESSION_PING,
            JsonRpcMethod.WC_SESSION_UPDATE
        )
        setupSequenceExpiration()
        propagatePendingSessionRequestsQueue()
    }

    fun setup() {
        jsonRpcInteractor.isConnectionAvailable
            .onEach { isAvailable -> _engineEvent.emit(ConnectionState(isAvailable)) }
            .filter { isAvailable: Boolean -> isAvailable }
            .onEach {
                supervisorScope {
                    launch(Dispatchers.IO) {
                        resubscribeToSession()
                    }
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

                if (signEventsJob == null) {
                    signEventsJob = collectSignEvents()
                }
            }.launchIn(scope)
    }

    internal suspend fun getVerifyContext(id: Long): EngineDO.VerifyContext? = verifyContextStorageRepository.get(id)?.toEngineDO()

    internal suspend fun getListOfVerifyContexts(): List<EngineDO.VerifyContext> = verifyContextStorageRepository.getAll().map { verifyContext -> verifyContext.toEngineDO() }

    private fun propagatePendingSessionRequestsQueue() {
        getPendingSessionRequests()
            .map { pendingRequest -> pendingRequest.toSessionRequest(metadataStorageRepository.getByTopicAndType(pendingRequest.topic, AppMetaDataType.PEER)) }
            .map { sessionRequest ->
                scope.launch {
                    supervisorScope {
                        val verifyContext = verifyContextStorageRepository.get(sessionRequest.request.id) ?: VerifyContext(sessionRequest.request.id, String.Empty, Validation.UNKNOWN, String.Empty)
                        val sessionRequestEvent = EngineDO.SessionRequestEvent(sessionRequest, verifyContext.toEngineDO())
                        sessionRequestsQueue.addLast(sessionRequestEvent)
                    }
                }
            }
    }

    private fun collectJsonRpcRequests(): Job =
        jsonRpcInteractor.clientSyncJsonRpc
            .filter { request -> request.params is SignParams }
            .onEach { request ->
                when (val requestParams = request.params) {
                    is SignParams.SessionProposeParams -> onSessionPropose(request, requestParams)
                    is SignParams.SessionSettleParams -> onSessionSettle(request, requestParams)
                    is SignParams.SessionRequestParams -> onSessionRequest(request, requestParams)
                    is SignParams.DeleteParams -> onSessionDelete(request, requestParams)
                    is SignParams.EventParams -> onSessionEvent(request, requestParams)
                    is SignParams.UpdateNamespacesParams -> onSessionUpdate(request, requestParams)
                    is SignParams.ExtendParams -> onSessionExtend(request, requestParams)
                    is SignParams.PingParams -> onPing(request)
                }
            }.launchIn(scope)

    private fun collectInternalErrors(): Job =
        merge(jsonRpcInteractor.internalErrors, pairingController.findWrongMethodsFlow, sessionRequestUseCase.errors)
            .onEach { exception -> _engineEvent.emit(exception) }
            .launchIn(scope)

    private fun collectSignEvents(): Job =
        merge(respondSessionRequestUseCase.events)
            .onEach { event -> _engineEvent.emit(event) }
            .launchIn(scope)

    private fun collectJsonRpcResponses(): Job =
        jsonRpcInteractor.peerResponse
            .filter { request -> request.params is SignParams }
            .onEach { response ->
                when (val params = response.params) {
                    is SignParams.SessionProposeParams -> onSessionProposalResponse(response, params)
                    is SignParams.SessionSettleParams -> onSessionSettleResponse(response)
                    is SignParams.UpdateNamespacesParams -> onSessionUpdateResponse(response)
                    is SignParams.SessionRequestParams -> onSessionRequestResponse(response, params)
                }
            }.launchIn(scope)

    // listened by WalletDelegate
    private fun onSessionPropose(request: WCRequest, payloadParams: SignParams.SessionProposeParams) {
        val irnParams = IrnParams(Tags.SESSION_PROPOSE_RESPONSE, Ttl(FIVE_MINUTES_IN_SECONDS))
        try {
            SignValidator.validateProposalNamespaces(payloadParams.requiredNamespaces) { error ->
                jsonRpcInteractor.respondWithError(request, error.toPeerError(), irnParams)
                return
            }

            SignValidator.validateProposalNamespaces(payloadParams.optionalNamespaces ?: emptyMap()) { error ->
                jsonRpcInteractor.respondWithError(request, error.toPeerError(), irnParams)
                return
            }

            payloadParams.properties?.let {
                SignValidator.validateProperties(payloadParams.properties) { error ->
                    jsonRpcInteractor.respondWithError(request, error.toPeerError(), irnParams)
                    return
                }
            }

            proposalStorageRepository.insertProposal(payloadParams.toVO(request.topic, request.id))
            pairingController.updateMetadata(Core.Params.UpdateMetadata(request.topic.value, payloadParams.proposer.metadata.toClient(), AppMetaDataType.PEER))
            val url = payloadParams.proposer.metadata.url
            val json = serializer.serialize(SignRpc.SessionPropose(id = request.id, params = payloadParams)) ?: throw Exception("Error serializing session proposal")
            resolveAttestationIdUseCase(request.id, json, url) { verifyContext ->
                val sessionProposalEvent = EngineDO.SessionProposalEvent(proposal = payloadParams.toEngineDO(request.topic), context = verifyContext.toEngineDO())
                scope.launch { _engineEvent.emit(sessionProposalEvent) }
            }
        } catch (e: Exception) {
            jsonRpcInteractor.respondWithError(
                request,
                Uncategorized.GenericError("Cannot handle a session proposal: ${e.message}, topic: ${request.topic}"),
                irnParams
            )
            scope.launch { _engineEvent.emit(SDKError(e)) }
        }
    }

    // listened by DappDelegate
    private fun onSessionSettle(request: WCRequest, settleParams: SignParams.SessionSettleParams) {
        val sessionTopic = request.topic
        val irnParams = IrnParams(Tags.SESSION_SETTLE_RESPONSE, Ttl(FIVE_MINUTES_IN_SECONDS))
        val selfPublicKey: PublicKey = try {
            crypto.getSelfPublicFromKeyAgreement(sessionTopic)
        } catch (e: Exception) {
            jsonRpcInteractor.respondWithError(request, PeerError.Failure.SessionSettlementFailed(e.message ?: String.Empty), irnParams)
            return
        }

        val peerMetadata = settleParams.controller.metadata
        val proposal = try {
            proposalStorageRepository.getProposalByKey(selfPublicKey.keyAsHex).also { proposalStorageRepository.deleteProposal(selfPublicKey.keyAsHex) }
        } catch (e: Exception) {
            jsonRpcInteractor.respondWithError(request, PeerError.Failure.SessionSettlementFailed(e.message ?: String.Empty), irnParams)
            return
        }

        val (requiredNamespaces, optionalNamespaces, properties) = proposal.run { Triple(requiredNamespaces, optionalNamespaces, properties) }
        SignValidator.validateSessionNamespace(settleParams.namespaces, requiredNamespaces) { error ->
            jsonRpcInteractor.respondWithError(request, error.toPeerError(), irnParams)
            return
        }

        scope.launch(Dispatchers.IO) {
            supervisorScope {
                try {
                    val session = SessionVO.createAcknowledgedSession(
                        sessionTopic,
                        settleParams,
                        selfPublicKey,
                        selfAppMetaData,
                        requiredNamespaces,
                        optionalNamespaces,
                        properties,
                        proposal.pairingTopic.value
                    )

                    sessionStorageRepository.insertSession(session, request.id)
                    pairingController.updateMetadata(Core.Params.UpdateMetadata(proposal.pairingTopic.value, peerMetadata.toClient(), AppMetaDataType.PEER))
                    metadataStorageRepository.insertOrAbortMetadata(sessionTopic, peerMetadata, AppMetaDataType.PEER)
                    jsonRpcInteractor.respondWithSuccess(request, irnParams)
                    _engineEvent.emit(session.toSessionApproved())
                } catch (e: Exception) {
                    proposalStorageRepository.insertProposal(proposal)
                    sessionStorageRepository.deleteSession(sessionTopic)
                    jsonRpcInteractor.respondWithError(request, PeerError.Failure.SessionSettlementFailed(e.message ?: String.Empty), irnParams)
                    scope.launch { _engineEvent.emit(SDKError(e)) }
                    return@supervisorScope
                }
            }
        }
    }

    // listened by both Delegates
    private fun onSessionDelete(request: WCRequest, params: SignParams.DeleteParams) {
        val irnParams = IrnParams(Tags.SESSION_DELETE_RESPONSE, Ttl(DAY_IN_SECONDS))
        try {
            if (!sessionStorageRepository.isSessionValid(request.topic)) {
                jsonRpcInteractor.respondWithError(
                    request,
                    Uncategorized.NoMatchingTopic(Sequences.SESSION.name, request.topic.value),
                    irnParams
                )
                return
            }

            jsonRpcInteractor.unsubscribe(request.topic,
                onSuccess = { crypto.removeKeys(request.topic.value) },
                onFailure = { error -> logger.error(error) })
            sessionStorageRepository.deleteSession(request.topic)

            scope.launch { _engineEvent.emit(params.toEngineDO(request.topic)) }
        } catch (e: Exception) {
            jsonRpcInteractor.respondWithError(
                request,
                Uncategorized.GenericError("Cannot delete a session: ${e.message}, topic: ${request.topic}"),
                irnParams
            )
            scope.launch { _engineEvent.emit(SDKError(e)) }
            return
        }
    }

    // listened by WalletDelegate
    private fun onSessionRequest(request: WCRequest, params: SignParams.SessionRequestParams) {
        val irnParams = IrnParams(Tags.SESSION_REQUEST_RESPONSE, Ttl(FIVE_MINUTES_IN_SECONDS))

        try {
            if (!CoreValidator.isExpiryWithinBounds(params.request.expiry)) {
                jsonRpcInteractor.respondWithError(request, Invalid.RequestExpired, irnParams)
                return
            }

            SignValidator.validateSessionRequest(params.toEngineDO(request.topic)) { error ->
                jsonRpcInteractor.respondWithError(request, error.toPeerError(), irnParams)
                return
            }

            if (!sessionStorageRepository.isSessionValid(request.topic)) {
                jsonRpcInteractor.respondWithError(
                    request,
                    Uncategorized.NoMatchingTopic(Sequences.SESSION.name, request.topic.value),
                    irnParams
                )
                return
            }
            val (sessionNamespaces: Map<String, NamespaceVO.Session>, sessionPeerAppMetaData: AppMetaData?) =
                sessionStorageRepository.getSessionWithoutMetadataByTopic(request.topic)
                    .run {
                        val peerAppMetaData = metadataStorageRepository.getByTopicAndType(this.topic, AppMetaDataType.PEER)
                        this.sessionNamespaces to peerAppMetaData
                    }

            val method = params.request.method
            SignValidator.validateChainIdWithMethodAuthorisation(params.chainId, method, sessionNamespaces) { error ->
                jsonRpcInteractor.respondWithError(request, error.toPeerError(), irnParams)
                return
            }

            val json = serializer.serialize(SignRpc.SessionRequest(id = request.id, params = params)) ?: throw Exception("Error serializing session request")
            val url = sessionPeerAppMetaData?.url ?: String.Empty

            resolveAttestationIdUseCase(request.id, json, url) { verifyContext ->
                val sessionRequestEvent = EngineDO.SessionRequestEvent(params.toEngineDO(request, sessionPeerAppMetaData), verifyContext.toEngineDO())
                val event = if (sessionRequestsQueue.isEmpty()) {
                    sessionRequestEvent
                } else {
                    sessionRequestsQueue.first()
                }

                sessionRequestsQueue.addLast(sessionRequestEvent)
                scope.launch { _engineEvent.emit(event) }
            }
        } catch (e: Exception) {
            jsonRpcInteractor.respondWithError(
                request,
                Uncategorized.GenericError("Cannot handle a session request: ${e.message}, topic: ${request.topic}"),
                irnParams
            )
            scope.launch { _engineEvent.emit(SDKError(e)) }
            return
        }
    }

    // listened by DappDelegate
    private fun onSessionEvent(request: WCRequest, params: SignParams.EventParams) {
        val irnParams = IrnParams(Tags.SESSION_EVENT_RESPONSE, Ttl(FIVE_MINUTES_IN_SECONDS))
        try {
            SignValidator.validateEvent(params.toEngineDOEvent()) { error ->
                jsonRpcInteractor.respondWithError(request, error.toPeerError(), irnParams)
                return
            }

            if (!sessionStorageRepository.isSessionValid(request.topic)) {
                jsonRpcInteractor.respondWithError(
                    request,
                    Uncategorized.NoMatchingTopic(Sequences.SESSION.name, request.topic.value),
                    irnParams
                )
                return
            }

            val session = sessionStorageRepository.getSessionWithoutMetadataByTopic(request.topic)
            if (!session.isPeerController) {
                jsonRpcInteractor.respondWithError(request, PeerError.Unauthorized.Event(Sequences.SESSION.name), irnParams)
                return
            }
            if (!session.isAcknowledged) {
                jsonRpcInteractor.respondWithError(
                    request,
                    Uncategorized.NoMatchingTopic(Sequences.SESSION.name, request.topic.value),
                    irnParams
                )
                return
            }

            val event = params.event
            SignValidator.validateChainIdWithEventAuthorisation(params.chainId, event.name, session.sessionNamespaces) { error ->
                jsonRpcInteractor.respondWithError(request, error.toPeerError(), irnParams)
                return
            }

            jsonRpcInteractor.respondWithSuccess(request, irnParams)
            scope.launch { _engineEvent.emit(params.toEngineDO(request.topic)) }
        } catch (e: Exception) {
            jsonRpcInteractor.respondWithError(
                request,
                Uncategorized.GenericError("Cannot emit an event: ${e.message}, topic: ${request.topic}"),
                irnParams
            )
            scope.launch { _engineEvent.emit(SDKError(e)) }
            return
        }
    }

    // listened by DappDelegate
    private fun onSessionUpdate(request: WCRequest, params: SignParams.UpdateNamespacesParams) {
        val irnParams = IrnParams(Tags.SESSION_UPDATE_RESPONSE, Ttl(DAY_IN_SECONDS))
        try {
            if (!sessionStorageRepository.isSessionValid(request.topic)) {
                jsonRpcInteractor.respondWithError(
                    request,
                    Uncategorized.NoMatchingTopic(Sequences.SESSION.name, request.topic.value),
                    irnParams
                )
                return
            }

            val session: SessionVO = sessionStorageRepository.getSessionWithoutMetadataByTopic(request.topic)
            if (!session.isPeerController) {
                jsonRpcInteractor.respondWithError(request, PeerError.Unauthorized.UpdateRequest(Sequences.SESSION.name), irnParams)
                return
            }

            SignValidator.validateSessionNamespace(params.namespaces, session.requiredNamespaces) { error ->
                jsonRpcInteractor.respondWithError(request, PeerError.Invalid.UpdateRequest(error.message), irnParams)
                return
            }

            if (!sessionStorageRepository.isUpdatedNamespaceValid(session.topic.value, request.id.extractTimestamp())) {
                jsonRpcInteractor.respondWithError(request, PeerError.Invalid.UpdateRequest("Update Namespace Request ID too old"), irnParams)
                return
            }

            sessionStorageRepository.deleteNamespaceAndInsertNewNamespace(session.topic.value, params.namespaces, request.id)
            jsonRpcInteractor.respondWithSuccess(request, irnParams)

            scope.launch {
                _engineEvent.emit(EngineDO.SessionUpdateNamespaces(request.topic, params.namespaces.toMapOfEngineNamespacesSession()))
            }
        } catch (e: Exception) {
            jsonRpcInteractor.respondWithError(
                request,
                PeerError.Invalid.UpdateRequest("Updating Namespace Failed. Review Namespace structure. Error: ${e.message}, topic: ${request.topic}"),
                irnParams
            )
            scope.launch { _engineEvent.emit(SDKError(e)) }
            return
        }
    }

    // listened by DappDelegate
    private fun onSessionExtend(request: WCRequest, requestParams: SignParams.ExtendParams) {
        val irnParams = IrnParams(Tags.SESSION_EXTEND_RESPONSE, Ttl(DAY_IN_SECONDS))
        try {
            if (!sessionStorageRepository.isSessionValid(request.topic)) {
                jsonRpcInteractor.respondWithError(
                    request,
                    Uncategorized.NoMatchingTopic(Sequences.SESSION.name, request.topic.value),
                    irnParams
                )
                return
            }

            val session = sessionStorageRepository.getSessionWithoutMetadataByTopic(request.topic)
            if (!session.isPeerController) {
                jsonRpcInteractor.respondWithError(request, PeerError.Unauthorized.ExtendRequest(Sequences.SESSION.name), irnParams)
                return
            }

            val newExpiry = requestParams.expiry
            SignValidator.validateSessionExtend(newExpiry, session.expiry.seconds) { error ->
                jsonRpcInteractor.respondWithError(request, error.toPeerError(), irnParams)
                return
            }

            sessionStorageRepository.extendSession(request.topic, newExpiry)
            jsonRpcInteractor.respondWithSuccess(request, irnParams)
            scope.launch { _engineEvent.emit(session.toEngineDOSessionExtend(Expiry(newExpiry))) }
        } catch (e: Exception) {
            jsonRpcInteractor.respondWithError(
                request,
                Uncategorized.GenericError("Cannot update a session: ${e.message}, topic: ${request.topic}"),
                irnParams
            )
            scope.launch { _engineEvent.emit(SDKError(e)) }
            return
        }
    }

    private fun onPing(request: WCRequest) {
        val irnParams = IrnParams(Tags.SESSION_PING_RESPONSE, Ttl(THIRTY_SECONDS))
        jsonRpcInteractor.respondWithSuccess(request, irnParams)
    }

    // listened by DappDelegate
    private fun onSessionProposalResponse(wcResponse: WCResponse, params: SignParams.SessionProposeParams) {
        try {
            val pairingTopic = wcResponse.topic
            pairingController.updateExpiry(Core.Params.UpdateExpiry(pairingTopic.value, Expiry(MONTH_IN_SECONDS)))
            pairingController.activate(Core.Params.Activate(pairingTopic.value))
            if (!pairingInterface.getPairings().any { pairing -> pairing.topic == pairingTopic.value }) return

            when (val response = wcResponse.response) {
                is JsonRpcResponse.JsonRpcResult -> {
                    logger.log("Session proposal approve received")
                    val selfPublicKey = PublicKey(params.proposer.publicKey)
                    val approveParams = response.result as CoreSignParams.ApprovalParams
                    val responderPublicKey = PublicKey(approveParams.responderPublicKey)
                    val sessionTopic = crypto.generateTopicFromKeyAgreement(selfPublicKey, responderPublicKey)
                    jsonRpcInteractor.subscribe(sessionTopic) { error ->
                        scope.launch {
                            _engineEvent.emit(SDKError(error))
                        }
                    }
                }

                is JsonRpcResponse.JsonRpcError -> {
                    logger.log("Session proposal reject received: ${response.error}")
                    proposalStorageRepository.deleteProposal(params.proposer.publicKey)
                    scope.launch { _engineEvent.emit(EngineDO.SessionRejected(pairingTopic.value, response.errorMessage)) }
                }
            }
        } catch (e: Exception) {
            scope.launch { _engineEvent.emit(SDKError(e)) }
        }
    }

    // listened by WalletDelegate
    private fun onSessionSettleResponse(wcResponse: WCResponse) {
        try {
            val sessionTopic = wcResponse.topic
            if (!sessionStorageRepository.isSessionValid(sessionTopic)) return
            val session = sessionStorageRepository.getSessionWithoutMetadataByTopic(sessionTopic).run {
                val peerAppMetaData = metadataStorageRepository.getByTopicAndType(this.topic, AppMetaDataType.PEER)
                this.copy(selfAppMetaData = selfAppMetaData, peerAppMetaData = peerAppMetaData)
            }

            when (wcResponse.response) {
                is JsonRpcResponse.JsonRpcResult -> {
                    logger.log("Session settle success received")
                    sessionStorageRepository.acknowledgeSession(sessionTopic)
                    scope.launch { _engineEvent.emit(EngineDO.SettledSessionResponse.Result(session.toEngineDO())) }
                }

                is JsonRpcResponse.JsonRpcError -> {
                    logger.error("Peer failed to settle session: ${(wcResponse.response as JsonRpcResponse.JsonRpcError).errorMessage}")
                    jsonRpcInteractor.unsubscribe(sessionTopic, onSuccess = {
                        sessionStorageRepository.deleteSession(sessionTopic)
                        crypto.removeKeys(sessionTopic.value)
                    })
                }
            }
        } catch (e: Exception) {
            scope.launch { _engineEvent.emit(SDKError(e)) }
        }
    }

    // listened by WalletDelegate
    private fun onSessionUpdateResponse(wcResponse: WCResponse) {
        try {
            val sessionTopic = wcResponse.topic
            if (!sessionStorageRepository.isSessionValid(sessionTopic)) return
            val session = sessionStorageRepository.getSessionWithoutMetadataByTopic(sessionTopic)
            if (!sessionStorageRepository.isUpdatedNamespaceResponseValid(session.topic.value, wcResponse.response.id.extractTimestamp())) {
                return
            }

            when (val response = wcResponse.response) {
                is JsonRpcResponse.JsonRpcResult -> {
                    logger.log("Session update namespaces response received")
                    val responseId = wcResponse.response.id
                    val namespaces = sessionStorageRepository.getTempNamespaces(responseId)
                    sessionStorageRepository.deleteNamespaceAndInsertNewNamespace(session.topic.value, namespaces, responseId)
                    sessionStorageRepository.markUnAckNamespaceAcknowledged(responseId)
                    scope.launch {
                        _engineEvent.emit(
                            EngineDO.SessionUpdateNamespacesResponse.Result(session.topic, session.sessionNamespaces.toMapOfEngineNamespacesSession())
                        )
                    }
                }

                is JsonRpcResponse.JsonRpcError -> {
                    logger.error("Peer failed to update session namespaces: ${response.error}")
                    scope.launch { _engineEvent.emit(EngineDO.SessionUpdateNamespacesResponse.Error(response.errorMessage)) }
                }
            }
        } catch (e: Exception) {
            scope.launch { _engineEvent.emit(SDKError(e)) }
        }
    }

    // listened by DappDelegate
    private fun onSessionRequestResponse(response: WCResponse, params: SignParams.SessionRequestParams) {
        try {
            val result = when (response.response) {
                is JsonRpcResponse.JsonRpcResult -> (response.response as JsonRpcResponse.JsonRpcResult).toEngineDO()
                is JsonRpcResponse.JsonRpcError -> (response.response as JsonRpcResponse.JsonRpcError).toEngineDO()
            }
            val method = params.request.method
            scope.launch { _engineEvent.emit(EngineDO.SessionPayloadResponse(response.topic.value, params.chainId, method, result)) }
        } catch (e: Exception) {
            scope.launch { _engineEvent.emit(SDKError(e)) }
        }
    }

    private fun resubscribeToSession() {
        try {
            val (listOfExpiredSession, listOfValidSessions) =
                sessionStorageRepository.getListOfSessionVOsWithoutMetadata().partition { session -> !session.expiry.isSequenceValid() }

            listOfExpiredSession
                .map { session -> session.topic }
                .onEach { sessionTopic ->
                    crypto.removeKeys(sessionTopic.value)
                    sessionStorageRepository.deleteSession(sessionTopic)
                }

            val validSessionTopics = listOfValidSessions.map { it.topic.value }
            jsonRpcInteractor.batchSubscribe(validSessionTopics) { error -> scope.launch { _engineEvent.emit(SDKError(error)) } }
        } catch (e: Exception) {
            scope.launch { _engineEvent.emit(SDKError(e)) }
        }
    }

    private fun setupSequenceExpiration() {
        try {
            sessionStorageRepository.onSessionExpired = { sessionTopic ->
                jsonRpcInteractor.unsubscribe(sessionTopic, onSuccess = {
                    sessionStorageRepository.deleteSession(sessionTopic)
                    crypto.removeKeys(sessionTopic.value)
                })
            }

            pairingController.topicExpiredFlow.onEach { topic ->
                sessionStorageRepository.getAllSessionTopicsByPairingTopic(topic).onEach { sessionTopic ->
                    jsonRpcInteractor.unsubscribe(Topic(sessionTopic), onSuccess = {
                        sessionStorageRepository.deleteSession(Topic(sessionTopic))
                        crypto.removeKeys(sessionTopic)
                    })
                }
            }.launchIn(scope)
        } catch (e: Exception) {
            scope.launch { _engineEvent.emit(SDKError(e)) }
        }
    }
}