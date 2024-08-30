@file:JvmSynthetic

package com.walletconnect.sign.engine.domain

import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.json_rpc.domain.link_mode.LinkModeJsonRpcInteractorInterface
import com.walletconnect.android.internal.common.model.AppMetaDataType
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.model.Validation
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.android.internal.common.model.type.RelayJsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.storage.metadata.MetadataStorageRepositoryInterface
import com.walletconnect.android.internal.common.storage.verify.VerifyContextStorageRepository
import com.walletconnect.android.internal.utils.CoreValidator.isExpired
import com.walletconnect.android.pairing.handler.PairingControllerInterface
import com.walletconnect.android.pulse.domain.InsertTelemetryEventUseCase
import com.walletconnect.android.pulse.model.EventType
import com.walletconnect.android.pulse.model.Trace
import com.walletconnect.android.pulse.model.properties.Properties
import com.walletconnect.android.pulse.model.properties.Props
import com.walletconnect.android.push.notifications.DecryptMessageUseCaseInterface
import com.walletconnect.android.relay.WSSConnectionState
import com.walletconnect.android.verify.model.VerifyContext
import com.walletconnect.foundation.util.Logger
import com.walletconnect.sign.common.model.vo.clientsync.session.params.SignParams
import com.walletconnect.sign.engine.model.EngineDO
import com.walletconnect.sign.engine.model.mapper.toEngineDO
import com.walletconnect.sign.engine.model.mapper.toExpiredProposal
import com.walletconnect.sign.engine.model.mapper.toExpiredSessionRequest
import com.walletconnect.sign.engine.model.mapper.toSessionRequest
import com.walletconnect.sign.engine.sessionRequestEventsQueue
import com.walletconnect.sign.engine.use_case.calls.ApproveSessionAuthenticateUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.ApproveSessionUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.DisconnectSessionUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.EmitEventUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.ExtendSessionUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.FormatAuthenticateMessageUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.GetListOfVerifyContextsUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.GetPairingsUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.GetPendingAuthenticateRequestUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.GetSessionProposalsUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.GetSessionsUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.GetVerifyContextByIdUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.PairUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.PingUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.ProposeSessionUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.RejectSessionAuthenticateUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.RejectSessionUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.RespondSessionRequestUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.SessionAuthenticateUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.SessionRequestUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.SessionUpdateUseCaseInterface
import com.walletconnect.sign.engine.use_case.requests.OnPingUseCase
import com.walletconnect.sign.engine.use_case.requests.OnSessionAuthenticateUseCase
import com.walletconnect.sign.engine.use_case.requests.OnSessionDeleteUseCase
import com.walletconnect.sign.engine.use_case.requests.OnSessionEventUseCase
import com.walletconnect.sign.engine.use_case.requests.OnSessionExtendUseCase
import com.walletconnect.sign.engine.use_case.requests.OnSessionProposalUseCase
import com.walletconnect.sign.engine.use_case.requests.OnSessionRequestUseCase
import com.walletconnect.sign.engine.use_case.requests.OnSessionSettleUseCase
import com.walletconnect.sign.engine.use_case.requests.OnSessionUpdateUseCase
import com.walletconnect.sign.engine.use_case.responses.OnSessionAuthenticateResponseUseCase
import com.walletconnect.sign.engine.use_case.responses.OnSessionProposalResponseUseCase
import com.walletconnect.sign.engine.use_case.responses.OnSessionRequestResponseUseCase
import com.walletconnect.sign.engine.use_case.responses.OnSessionSettleResponseUseCase
import com.walletconnect.sign.engine.use_case.responses.OnSessionUpdateResponseUseCase
import com.walletconnect.sign.json_rpc.domain.DeleteRequestByIdUseCase
import com.walletconnect.sign.json_rpc.domain.GetPendingRequestsUseCaseByTopicInterface
import com.walletconnect.sign.json_rpc.domain.GetPendingSessionRequestByTopicUseCaseInterface
import com.walletconnect.sign.json_rpc.domain.GetPendingSessionRequests
import com.walletconnect.sign.json_rpc.model.JsonRpcMethod
import com.walletconnect.sign.storage.authenticate.AuthenticateResponseTopicRepository
import com.walletconnect.sign.storage.proposal.ProposalStorageRepository
import com.walletconnect.sign.storage.sequence.SessionStorageRepository
import com.walletconnect.utils.Empty
import com.walletconnect.utils.isSequenceValid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

internal class SignEngine(
    private val jsonRpcInteractor: RelayJsonRpcInteractorInterface,
    private val getPendingRequestsByTopicUseCase: GetPendingRequestsUseCaseByTopicInterface,
    private val getPendingSessionRequestByTopicUseCase: GetPendingSessionRequestByTopicUseCaseInterface,
    private val getPendingSessionRequests: GetPendingSessionRequests,
    private val getPendingAuthenticateRequestUseCase: GetPendingAuthenticateRequestUseCaseInterface,
    private val deleteRequestByIdUseCase: DeleteRequestByIdUseCase,
    private val crypto: KeyManagementRepository,
    private val authenticateResponseTopicRepository: AuthenticateResponseTopicRepository,
    private val proposalStorageRepository: ProposalStorageRepository,
    private val sessionStorageRepository: SessionStorageRepository,
    private val metadataStorageRepository: MetadataStorageRepositoryInterface,
    private val pairingController: PairingControllerInterface,
    private val verifyContextStorageRepository: VerifyContextStorageRepository,
    private val proposeSessionUseCase: ProposeSessionUseCaseInterface,
    private val authenticateSessionUseCase: SessionAuthenticateUseCaseInterface,
    private val pairUseCase: PairUseCaseInterface,
    private val rejectSessionUseCase: RejectSessionUseCaseInterface,
    private val approveSessionUseCase: ApproveSessionUseCaseInterface,
    private val approveSessionAuthenticateUseCase: ApproveSessionAuthenticateUseCaseInterface,
    private val rejectSessionAuthenticateUseCase: RejectSessionAuthenticateUseCaseInterface,
    private val sessionUpdateUseCase: SessionUpdateUseCaseInterface,
    private val sessionRequestUseCase: SessionRequestUseCaseInterface,
    private val respondSessionRequestUseCase: RespondSessionRequestUseCaseInterface,
    private val pingUseCase: PingUseCaseInterface,
    private val formatAuthenticateMessageUseCase: FormatAuthenticateMessageUseCaseInterface,
    private val emitEventUseCase: EmitEventUseCaseInterface,
    private val extendSessionUseCase: ExtendSessionUseCaseInterface,
    private val disconnectSessionUseCase: DisconnectSessionUseCaseInterface,
    private val decryptMessageUseCase: DecryptMessageUseCaseInterface,
    private val getSessionsUseCase: GetSessionsUseCaseInterface,
    private val getPairingsUseCase: GetPairingsUseCaseInterface,
    private val getSessionProposalsUseCase: GetSessionProposalsUseCaseInterface,
    private val getVerifyContextByIdUseCase: GetVerifyContextByIdUseCaseInterface,
    private val getListOfVerifyContextsUseCase: GetListOfVerifyContextsUseCaseInterface,
    private val onSessionProposeUse: OnSessionProposalUseCase,
    private val onAuthenticateSessionUseCase: OnSessionAuthenticateUseCase,
    private val onSessionSettleUseCase: OnSessionSettleUseCase,
    private val onSessionRequestUseCase: OnSessionRequestUseCase,
    private val onSessionDeleteUseCase: OnSessionDeleteUseCase,
    private val onSessionEventUseCase: OnSessionEventUseCase,
    private val onSessionUpdateUseCase: OnSessionUpdateUseCase,
    private val onSessionExtendUseCase: OnSessionExtendUseCase,
    private val onPingUseCase: OnPingUseCase,
    private val onSessionProposalResponseUseCase: OnSessionProposalResponseUseCase,
    private val onSessionAuthenticateResponseUseCase: OnSessionAuthenticateResponseUseCase,
    private val onSessionSettleResponseUseCase: OnSessionSettleResponseUseCase,
    private val onSessionUpdateResponseUseCase: OnSessionUpdateResponseUseCase,
    private val onSessionRequestResponseUseCase: OnSessionRequestResponseUseCase,
    private val insertEventUseCase: InsertTelemetryEventUseCase,
    private val linkModeJsonRpcInteractor: LinkModeJsonRpcInteractorInterface,
    private val logger: Logger
) : ProposeSessionUseCaseInterface by proposeSessionUseCase,
    SessionAuthenticateUseCaseInterface by authenticateSessionUseCase,
    PairUseCaseInterface by pairUseCase,
    RejectSessionUseCaseInterface by rejectSessionUseCase,
    ApproveSessionUseCaseInterface by approveSessionUseCase,
    ApproveSessionAuthenticateUseCaseInterface by approveSessionAuthenticateUseCase,
    RejectSessionAuthenticateUseCaseInterface by rejectSessionAuthenticateUseCase,
    SessionUpdateUseCaseInterface by sessionUpdateUseCase,
    SessionRequestUseCaseInterface by sessionRequestUseCase,
    RespondSessionRequestUseCaseInterface by respondSessionRequestUseCase,
    PingUseCaseInterface by pingUseCase,
    FormatAuthenticateMessageUseCaseInterface by formatAuthenticateMessageUseCase,
    EmitEventUseCaseInterface by emitEventUseCase,
    ExtendSessionUseCaseInterface by extendSessionUseCase,
    DisconnectSessionUseCaseInterface by disconnectSessionUseCase,
    DecryptMessageUseCaseInterface by decryptMessageUseCase,
    GetSessionsUseCaseInterface by getSessionsUseCase,
    GetPairingsUseCaseInterface by getPairingsUseCase,
    GetPendingRequestsUseCaseByTopicInterface by getPendingRequestsByTopicUseCase,
    GetPendingAuthenticateRequestUseCaseInterface by getPendingAuthenticateRequestUseCase,
    GetPendingSessionRequestByTopicUseCaseInterface by getPendingSessionRequestByTopicUseCase,
    GetSessionProposalsUseCaseInterface by getSessionProposalsUseCase,
    GetVerifyContextByIdUseCaseInterface by getVerifyContextByIdUseCase,
    GetListOfVerifyContextsUseCaseInterface by getListOfVerifyContextsUseCase,
    LinkModeJsonRpcInteractorInterface by linkModeJsonRpcInteractor {
    private var jsonRpcRequestsJob: Job? = null
    private var jsonRpcResponsesJob: Job? = null

    private var internalErrorsJob: Job? = null
    private var signEventsJob: Job? = null

    private var envelopeRequestsJob: Job? = null
    private var envelopeResponsesJob: Job? = null

    private val _engineEvent: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val engineEvent: SharedFlow<EngineEvent> = _engineEvent.asSharedFlow()
    val wssConnection: StateFlow<WSSConnectionState> = jsonRpcInteractor.wssConnectionState

    init {
        pairingController.register(
            JsonRpcMethod.WC_SESSION_PROPOSE,
            JsonRpcMethod.WC_SESSION_AUTHENTICATE,
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
        emitReceivedPendingRequestsWhilePairingOnTheSameURL()
        sessionProposalExpiryWatcher()
        sessionRequestsExpiryWatcher()
    }

    fun setup() {
        handleLinkModeRequests()
        handleLinkModeResponses()

        if (signEventsJob == null) {
            signEventsJob = collectSignEvents()
        }

        if (internalErrorsJob == null) {
            internalErrorsJob = collectInternalErrors()
        }

        handleRelayRequestsAndResponses()
    }

    private fun handleRelayRequestsAndResponses() {
        jsonRpcInteractor.onResubscribe
            .onEach {
                scope.launch {
                    supervisorScope {
                        launch(Dispatchers.IO) {
                            resubscribeToSession()
                            resubscribeToPendingAuthenticateTopics()
                        }
                    }

                    if (jsonRpcRequestsJob == null) {
                        jsonRpcRequestsJob = collectJsonRpcRequests()
                    }

                    if (jsonRpcResponsesJob == null) {
                        jsonRpcResponsesJob = collectJsonRpcResponses()
                    }
                }
            }.launchIn(scope)
    }

    private fun handleLinkModeResponses() {
        if (envelopeResponsesJob == null) {
            envelopeResponsesJob = linkModeJsonRpcInteractor.peerResponse
                .filter { request -> request.params is SignParams }
                .onEach { response ->
                    when (val params = response.params) {
                        is SignParams.SessionAuthenticateParams -> onSessionAuthenticateResponseUseCase(response, params)
                        is SignParams.SessionRequestParams -> onSessionRequestResponseUseCase(response, params)
                    }
                }.launchIn(scope)
        }
    }

    private fun handleLinkModeRequests() {
        if (envelopeRequestsJob == null) {
            envelopeRequestsJob = linkModeJsonRpcInteractor.clientSyncJsonRpc
                .filter { request -> request.params is SignParams }
                .onEach { request ->
                    when (val requestParams = request.params) {
                        is SignParams.SessionAuthenticateParams -> onAuthenticateSessionUseCase(request, requestParams)
                        is SignParams.SessionRequestParams -> onSessionRequestUseCase(request, requestParams)
                    }
                }.launchIn(scope)
        }
    }

    private fun collectJsonRpcRequests(): Job =
        jsonRpcInteractor.clientSyncJsonRpc
            .filter { request -> request.params is SignParams }
            .onEach { request ->
                when (val requestParams = request.params) {
                    is SignParams.SessionProposeParams -> onSessionProposeUse(request, requestParams)
                    is SignParams.SessionAuthenticateParams -> onAuthenticateSessionUseCase(request, requestParams)
                    is SignParams.SessionSettleParams -> onSessionSettleUseCase(request, requestParams)
                    is SignParams.SessionRequestParams -> onSessionRequestUseCase(request, requestParams)
                    is SignParams.DeleteParams -> onSessionDeleteUseCase(request, requestParams)
                    is SignParams.EventParams -> onSessionEventUseCase(request, requestParams)
                    is SignParams.UpdateNamespacesParams -> onSessionUpdateUseCase(request, requestParams)
                    is SignParams.ExtendParams -> onSessionExtendUseCase(request, requestParams)
                    is SignParams.PingParams -> onPingUseCase(request)
                }
            }.launchIn(scope)

    private fun collectJsonRpcResponses(): Job =
        jsonRpcInteractor.peerResponse
            .filter { request -> request.params is SignParams }
            .onEach { response ->
                when (val params = response.params) {
                    is SignParams.SessionProposeParams -> onSessionProposalResponseUseCase(response, params)
                    is SignParams.SessionAuthenticateParams -> onSessionAuthenticateResponseUseCase(response, params)
                    is SignParams.SessionSettleParams -> onSessionSettleResponseUseCase(response)
                    is SignParams.UpdateNamespacesParams -> onSessionUpdateResponseUseCase(response)
                    is SignParams.SessionRequestParams -> onSessionRequestResponseUseCase(response, params)
                }
            }.launchIn(scope)

    private fun collectInternalErrors(): Job =
        merge(jsonRpcInteractor.internalErrors, linkModeJsonRpcInteractor.internalErrors, pairingController.findWrongMethodsFlow, sessionRequestUseCase.errors)
            .onEach { exception -> _engineEvent.emit(exception) }
            .launchIn(scope)

    private fun collectSignEvents(): Job =
        merge(
            respondSessionRequestUseCase.events,
            onSessionRequestUseCase.events,
            onSessionDeleteUseCase.events,
            onSessionProposeUse.events,
            onAuthenticateSessionUseCase.events,
            onSessionEventUseCase.events,
            onSessionSettleUseCase.events,
            onSessionUpdateUseCase.events,
            onSessionExtendUseCase.events,
            onSessionProposalResponseUseCase.events,
            onSessionSettleResponseUseCase.events,
            onSessionUpdateResponseUseCase.events,
            onSessionRequestResponseUseCase.events,
            onSessionAuthenticateResponseUseCase.events
        )
            .onEach { event -> _engineEvent.emit(event) }
            .launchIn(scope)

    private fun resubscribeToSession() {
        try {
            val (listOfExpiredSession, listOfValidSessions) =
                sessionStorageRepository.getListOfSessionVOsWithoutMetadata().partition { session -> !session.expiry.isSequenceValid() }

            listOfExpiredSession
                .map { session -> session.topic }
                .onEach { sessionTopic ->
                    runCatching { crypto.removeKeys(sessionTopic.value) }.onFailure { logger.error(it) }
                    sessionStorageRepository.deleteSession(sessionTopic)
                }

            val validSessionTopics = listOfValidSessions.map { it.topic.value }
            jsonRpcInteractor.batchSubscribe(validSessionTopics) { error -> scope.launch { _engineEvent.emit(SDKError(error)) } }
        } catch (e: Exception) {
            scope.launch { _engineEvent.emit(SDKError(e)) }
        }
    }

    private fun resubscribeToPendingAuthenticateTopics() {
        scope.launch {
            try {
                val responseTopics = authenticateResponseTopicRepository.getResponseTopics().map { responseTopic -> responseTopic }
                jsonRpcInteractor.batchSubscribe(responseTopics) { error -> scope.launch { _engineEvent.emit(SDKError(error)) } }
            } catch (e: Exception) {
                scope.launch { _engineEvent.emit(SDKError(e)) }
            }
        }
    }

    private fun setupSequenceExpiration() {
        try {
            sessionStorageRepository.onSessionExpired = { sessionTopic ->
                jsonRpcInteractor.unsubscribe(sessionTopic, onSuccess = {
                    runCatching {
                        sessionStorageRepository.deleteSession(sessionTopic)
                        crypto.removeKeys(sessionTopic.value)
                    }.onFailure { logger.error(it) }
                })
            }
        } catch (e: Exception) {
            scope.launch { _engineEvent.emit(SDKError(e)) }
        }
    }

    private fun propagatePendingSessionRequestsQueue() = scope.launch {
        getPendingSessionRequests()
            .map { pendingRequest -> pendingRequest.toSessionRequest(metadataStorageRepository.getByTopicAndType(pendingRequest.topic, AppMetaDataType.PEER)) }
            .filter { sessionRequest -> sessionRequest.expiry?.isExpired() == false }
            .filter { sessionRequest -> getSessionsUseCase.getListOfSettledSessions().find { session -> session.topic.value == sessionRequest.topic } != null }
            .onEach { sessionRequest ->
                scope.launch {
                    supervisorScope {
                        val verifyContext =
                            verifyContextStorageRepository.get(sessionRequest.request.id) ?: VerifyContext(
                                sessionRequest.request.id,
                                String.Empty,
                                Validation.UNKNOWN,
                                String.Empty,
                                null
                            )
                        val sessionRequestEvent = EngineDO.SessionRequestEvent(sessionRequest, verifyContext.toEngineDO())
                        sessionRequestEventsQueue.add(sessionRequestEvent)
                    }
                }
            }
    }

    private fun sessionProposalExpiryWatcher() {
        repeatableFlow()
            .onEach {
                try {
                    proposalStorageRepository
                        .getProposals()
                        .onEach { proposal ->
                            proposal.expiry?.let {
                                if (it.isExpired()) {
                                    proposalStorageRepository.deleteProposal(proposal.proposerPublicKey)
                                    deleteRequestByIdUseCase(proposal.requestId)
                                    _engineEvent.emit(proposal.toExpiredProposal())
                                }
                            }
                        }
                } catch (e: Exception) {
                    logger.error(e)
                }
            }.launchIn(scope)
    }

    private fun sessionRequestsExpiryWatcher() {
        repeatableFlow()
            .onEach {
                try {
                    getPendingSessionRequests()
                        .onEach { pendingRequest ->
                            pendingRequest.expiry?.let {
                                if (it.isExpired()) {
                                    deleteRequestByIdUseCase(pendingRequest.id)
                                    _engineEvent.emit(pendingRequest.toExpiredSessionRequest())
                                }
                            }
                        }
                } catch (e: Exception) {
                    logger.error(e)
                }
            }.launchIn(scope)
    }

    private fun emitReceivedPendingRequestsWhilePairingOnTheSameURL() {
        pairingController.storedPairingFlow
            .onEach { (pairingTopic, trace) ->
                try {
                    val pendingAuthenticateRequests = getPendingAuthenticateRequestUseCase.getPendingAuthenticateRequests().filter { request -> request.topic == pairingTopic }
                    if (pendingAuthenticateRequests.isNotEmpty()) {
                        pendingAuthenticateRequests.forEach { request ->
                            val context = verifyContextStorageRepository.get(request.id) ?: VerifyContext(request.id, String.Empty, Validation.UNKNOWN, String.Empty, null)
                            val sessionAuthenticateEvent = EngineDO.SessionAuthenticateEvent(
                                request.id,
                                request.topic.value,
                                request.params.authPayload.toEngineDO(),
                                request.params.requester.toEngineDO(),
                                request.params.expiryTimestamp,
                                context.toEngineDO()
                            )
                            logger.log("Emitting pending authenticate request from active pairing: $sessionAuthenticateEvent")
                            scope.launch { _engineEvent.emit(sessionAuthenticateEvent) }
                        }
                    } else {
                        val proposal = proposalStorageRepository.getProposalByTopic(pairingTopic.value)
                        if (proposal.expiry?.isExpired() == true) {
                            insertEventUseCase(Props(type = EventType.Error.PROPOSAL_EXPIRED, properties = Properties(trace = trace, topic = pairingTopic.value)))
                            proposalStorageRepository.deleteProposal(proposal.proposerPublicKey)
                            scope.launch { _engineEvent.emit(proposal.toExpiredProposal()) }
                        } else {
                            val context = verifyContextStorageRepository.get(proposal.requestId) ?: VerifyContext(proposal.requestId, String.Empty, Validation.UNKNOWN, String.Empty, null)
                            val sessionProposalEvent = EngineDO.SessionProposalEvent(proposal = proposal.toEngineDO(), context = context.toEngineDO())
                            logger.log("Emitting session proposal from active pairing: $sessionProposalEvent")
                            trace.add(Trace.Pairing.EMIT_SESSION_PROPOSAL)
                            scope.launch { _engineEvent.emit(sessionProposalEvent) }
                        }
                    }
                } catch (e: Exception) {
                    logger.log("No proposal or pending session authenticate request for pairing topic: $e")
                    scope.launch { _engineEvent.emit(SDKError(Throwable("No proposal or pending session authenticate request for pairing topic: $e"))) }
                }
            }.launchIn(scope)
    }

    private fun repeatableFlow() = flow {
        while (true) {
            emit(Unit)
            delay(WATCHER_INTERVAL)
        }
    }

    companion object {
        private const val WATCHER_INTERVAL = 30000L //30s
    }
}