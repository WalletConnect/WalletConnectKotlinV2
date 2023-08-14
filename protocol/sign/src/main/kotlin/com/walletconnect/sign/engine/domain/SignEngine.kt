@file:JvmSynthetic

package com.walletconnect.sign.engine.domain

import com.walletconnect.android.Core
import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.exception.Invalid
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
import com.walletconnect.android.pairing.client.PairingInterface
import com.walletconnect.android.pairing.handler.PairingControllerInterface
import com.walletconnect.android.pairing.model.mapper.toClient
import com.walletconnect.android.verify.data.model.VerifyContext
import com.walletconnect.android.verify.domain.ResolveAttestationIdUseCase
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.foundation.util.Logger
import com.walletconnect.sign.common.exceptions.PeerError
import com.walletconnect.sign.common.model.type.Sequences
import com.walletconnect.sign.common.model.vo.clientsync.common.NamespaceVO
import com.walletconnect.sign.common.model.vo.clientsync.session.SignRpc
import com.walletconnect.sign.common.model.vo.clientsync.session.params.SignParams
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
import com.walletconnect.sign.engine.use_case.calls.ApproveSessionUseCase
import com.walletconnect.sign.engine.use_case.calls.ApproveSessionUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.DisconnectSessionUseCase
import com.walletconnect.sign.engine.use_case.calls.DisconnectSessionUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.EmitEventUseCase
import com.walletconnect.sign.engine.use_case.calls.EmitEventUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.ExtendSessionUsesCase
import com.walletconnect.sign.engine.use_case.calls.ExtendSessionUsesCaseInterface
import com.walletconnect.sign.engine.use_case.calls.GetListOfVerifyContextsUseCase
import com.walletconnect.sign.engine.use_case.calls.GetListOfVerifyContextsUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.GetPairingsUseCase
import com.walletconnect.sign.engine.use_case.calls.GetPairingsUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.GetSessionProposalsUseCase
import com.walletconnect.sign.engine.use_case.calls.GetSessionProposalsUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.GetSessionsUseCase
import com.walletconnect.sign.engine.use_case.calls.GetSessionsUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.GetVerifyContextByIdUseCase
import com.walletconnect.sign.engine.use_case.calls.GetVerifyContextByIdUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.PairUseCase
import com.walletconnect.sign.engine.use_case.calls.PairUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.PingUseCase
import com.walletconnect.sign.engine.use_case.calls.PingUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.ProposeSessionUseCase
import com.walletconnect.sign.engine.use_case.calls.ProposeSessionUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.RejectSessionUseCase
import com.walletconnect.sign.engine.use_case.calls.RejectSessionUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.RespondSessionRequestUseCase
import com.walletconnect.sign.engine.use_case.calls.RespondSessionRequestUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.SessionRequestUseCase
import com.walletconnect.sign.engine.use_case.calls.SessionRequestUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.SessionUpdateUseCase
import com.walletconnect.sign.engine.use_case.calls.SessionUpdateUseCaseInterface
import com.walletconnect.sign.engine.use_case.requests.OnPingUseCase
import com.walletconnect.sign.engine.use_case.requests.OnSessionDeleteUseCase
import com.walletconnect.sign.engine.use_case.requests.OnSessionEventUseCase
import com.walletconnect.sign.engine.use_case.requests.OnSessionExtendUseCase
import com.walletconnect.sign.engine.use_case.requests.OnSessionProposeUseCase
import com.walletconnect.sign.engine.use_case.requests.OnSessionRequestUseCase
import com.walletconnect.sign.engine.use_case.requests.OnSessionSettleUseCase
import com.walletconnect.sign.engine.use_case.requests.OnSessionUpdateUseCase
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
    private val getVerifyContextByIdUseCase: GetVerifyContextByIdUseCase,
    private val getListOfVerifyContextsUseCase: GetListOfVerifyContextsUseCase,
    private val onSessionProposeUse: OnSessionProposeUseCase,
    private val onSessionSettleUseCase: OnSessionSettleUseCase,
    private val onSessionRequestUseCase: OnSessionRequestUseCase,
    private val onSessionDeleteUseCase: OnSessionDeleteUseCase,
    private val onSessionEventUseCase: OnSessionEventUseCase,
    private val onSessionUpdateUseCase: OnSessionUpdateUseCase,
    private val onSessionExtendUseCase: OnSessionExtendUseCase,
    private val onPingUseCase: OnPingUseCase,
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
            onSessionUpdateUseCase.events
        )
            .onEach { event -> _engineEvent.emit(event) }
            .launchIn(scope)

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
                    is SignParams.SessionProposeParams -> onSessionProposalResponse(response, params)
                    is SignParams.SessionSettleParams -> onSessionSettleResponse(response)
                    is SignParams.UpdateNamespacesParams -> onSessionUpdateResponse(response)
                    is SignParams.SessionRequestParams -> onSessionRequestResponse(response, params)
                }
            }.launchIn(scope)

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