@file:JvmSynthetic

package com.walletconnect.sign.engine.domain

import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.model.AppMetaDataType
import com.walletconnect.android.internal.common.model.ConnectionState
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.model.Validation
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.storage.MetadataStorageRepositoryInterface
import com.walletconnect.android.internal.common.storage.VerifyContextStorageRepository
import com.walletconnect.android.internal.utils.CoreValidator
import com.walletconnect.android.pairing.handler.PairingControllerInterface
import com.walletconnect.android.verify.data.model.VerifyContext
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.sign.common.model.vo.clientsync.session.params.SignParams
import com.walletconnect.sign.engine.model.EngineDO
import com.walletconnect.sign.engine.model.mapper.toEngineDO
import com.walletconnect.sign.engine.model.mapper.toSessionRequest
import com.walletconnect.sign.engine.sessionRequestEvetnsQueue
import com.walletconnect.sign.engine.use_case.calls.ApproveSessionUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.DisconnectSessionUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.EmitEventUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.ExtendSessionUsesCaseInterface
import com.walletconnect.sign.engine.use_case.calls.GetListOfVerifyContextsUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.GetPairingsUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.GetSessionProposalsUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.GetSessionsUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.GetVerifyContextByIdUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.PairUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.PingUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.ProposeSessionUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.RejectSessionUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.RespondSessionRequestUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.SessionRequestUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.SessionUpdateUseCaseInterface
import com.walletconnect.sign.engine.use_case.requests.OnPingUseCase
import com.walletconnect.sign.engine.use_case.requests.OnSessionDeleteUseCase
import com.walletconnect.sign.engine.use_case.requests.OnSessionEventUseCase
import com.walletconnect.sign.engine.use_case.requests.OnSessionExtendUseCase
import com.walletconnect.sign.engine.use_case.requests.OnSessionProposeUseCase
import com.walletconnect.sign.engine.use_case.requests.OnSessionRequestUseCase
import com.walletconnect.sign.engine.use_case.requests.OnSessionSettleUseCase
import com.walletconnect.sign.engine.use_case.requests.OnSessionUpdateUseCase
import com.walletconnect.sign.engine.use_case.responses.OnSessionProposalResponseUseCase
import com.walletconnect.sign.engine.use_case.responses.OnSessionRequestResponseUseCase
import com.walletconnect.sign.engine.use_case.responses.OnSessionSettleResponseUseCase
import com.walletconnect.sign.engine.use_case.responses.OnSessionUpdateResponseUseCase
import com.walletconnect.sign.json_rpc.domain.GetPendingRequestsUseCaseByTopicInterface
import com.walletconnect.sign.json_rpc.domain.GetPendingSessionRequestByTopicUseCaseInterface
import com.walletconnect.sign.json_rpc.domain.GetPendingSessionRequests
import com.walletconnect.sign.json_rpc.model.JsonRpcMethod
import com.walletconnect.sign.storage.sequence.SessionStorageRepository
import com.walletconnect.utils.Empty
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
    private val getPendingRequestsByTopicUseCase: GetPendingRequestsUseCaseByTopicInterface,
    private val getPendingSessionRequestByTopicUseCase: GetPendingSessionRequestByTopicUseCaseInterface,
    private val getPendingSessionRequests: GetPendingSessionRequests,
    private val crypto: KeyManagementRepository,
    private val sessionStorageRepository: SessionStorageRepository,
    private val metadataStorageRepository: MetadataStorageRepositoryInterface,
    private val pairingController: PairingControllerInterface,
    private val verifyContextStorageRepository: VerifyContextStorageRepository,
    private val proposeSessionUseCase: ProposeSessionUseCaseInterface,
    private val pairUseCase: PairUseCaseInterface,
    private val rejectSessionUseCase: RejectSessionUseCaseInterface,
    private val approveSessionUseCase: ApproveSessionUseCaseInterface,
    private val sessionUpdateUseCase: SessionUpdateUseCaseInterface,
    private val sessionRequestUseCase: SessionRequestUseCaseInterface,
    private val respondSessionRequestUseCase: RespondSessionRequestUseCaseInterface,
    private val pingUseCase: PingUseCaseInterface,
    private val emitEventUseCase: EmitEventUseCaseInterface,
    private val extendSessionUsesCase: ExtendSessionUsesCaseInterface,
    private val disconnectSessionUseCase: DisconnectSessionUseCaseInterface,
    private val getSessionsUseCase: GetSessionsUseCaseInterface,
    private val getPairingsUseCase: GetPairingsUseCaseInterface,
    private val getSessionProposalsUseCase: GetSessionProposalsUseCaseInterface,
    private val getVerifyContextByIdUseCase: GetVerifyContextByIdUseCaseInterface,
    private val getListOfVerifyContextsUseCase: GetListOfVerifyContextsUseCaseInterface,
    private val onSessionProposeUse: OnSessionProposeUseCase,
    private val onSessionSettleUseCase: OnSessionSettleUseCase,
    private val onSessionRequestUseCase: OnSessionRequestUseCase,
    private val onSessionDeleteUseCase: OnSessionDeleteUseCase,
    private val onSessionEventUseCase: OnSessionEventUseCase,
    private val onSessionUpdateUseCase: OnSessionUpdateUseCase,
    private val onSessionExtendUseCase: OnSessionExtendUseCase,
    private val onPingUseCase: OnPingUseCase,
    private val onSessionProposalResponseUseCase: OnSessionProposalResponseUseCase,
    private val onSessionSettleResponseUseCase: OnSessionSettleResponseUseCase,
    private val onSessionUpdateResponseUseCase: OnSessionUpdateResponseUseCase,
    private val onSessionRequestResponseUseCase: OnSessionRequestResponseUseCase,
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
    GetPendingSessionRequestByTopicUseCaseInterface by getPendingSessionRequestByTopicUseCase,
    GetSessionProposalsUseCaseInterface by getSessionProposalsUseCase,
    GetVerifyContextByIdUseCaseInterface by getVerifyContextByIdUseCase,
    GetListOfVerifyContextsUseCaseInterface by getListOfVerifyContextsUseCase {
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

    private fun collectJsonRpcRequests(): Job =
        jsonRpcInteractor.clientSyncJsonRpc
            .filter { request -> request.params is SignParams }
            .onEach { request ->
                when (val requestParams = request.params) {
                    is SignParams.SessionProposeParams -> onSessionProposeUse(request, requestParams)
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
                    is SignParams.SessionSettleParams -> onSessionSettleResponseUseCase(response)
                    is SignParams.UpdateNamespacesParams -> onSessionUpdateResponseUseCase(response)
                    is SignParams.SessionRequestParams -> onSessionRequestResponseUseCase(response, params)
                }
            }.launchIn(scope)

    private fun collectInternalErrors(): Job =
        merge(jsonRpcInteractor.internalErrors, pairingController.findWrongMethodsFlow, sessionRequestUseCase.errors)
            .onEach { exception -> _engineEvent.emit(exception) }
            .launchIn(scope)

    private fun collectSignEvents(): Job =
        merge(
            respondSessionRequestUseCase.events,
            onSessionRequestUseCase.events,
            onSessionDeleteUseCase.events,
            onSessionProposeUse.events,
            onSessionEventUseCase.events,
            onSessionSettleUseCase.events,
            onSessionUpdateUseCase.events,
            onSessionProposalResponseUseCase.events,
            onSessionSettleResponseUseCase.events,
            onSessionUpdateResponseUseCase.events,
            onSessionRequestResponseUseCase.events
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

    private fun propagatePendingSessionRequestsQueue() = scope.launch {
        getPendingSessionRequests()
            .map { pendingRequest -> pendingRequest.toSessionRequest(metadataStorageRepository.getByTopicAndType(pendingRequest.topic, AppMetaDataType.PEER)) }
            .onEach { sessionRequest ->
                if (CoreValidator.isExpiryWithinBounds(sessionRequest.expiry)) {
                    scope.launch {
                        supervisorScope {
                            val verifyContext =
                                verifyContextStorageRepository.get(sessionRequest.request.id) ?: VerifyContext(sessionRequest.request.id, String.Empty, Validation.UNKNOWN, String.Empty)
                            val sessionRequestEvent = EngineDO.SessionRequestEvent(sessionRequest, verifyContext.toEngineDO())
                            sessionRequestEvetnsQueue.addLast(sessionRequestEvent)
                        }
                    }
                }
            }
    }
}