@file:JvmSynthetic

package com.walletconnect.sign.engine.domain

import android.database.sqlite.SQLiteException
import com.walletconnect.android.Core
import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.exception.CannotFindSequenceForTopic
import com.walletconnect.android.internal.common.exception.GenericException
import com.walletconnect.android.internal.common.exception.Invalid
import com.walletconnect.android.internal.common.exception.InvalidExpiryException
import com.walletconnect.android.internal.common.exception.Reason
import com.walletconnect.android.internal.common.exception.Uncategorized
import com.walletconnect.android.internal.common.json_rpc.data.JsonRpcSerializer
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.AppMetaDataType
import com.walletconnect.android.internal.common.model.ConnectionState
import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.Pairing
import com.walletconnect.android.internal.common.model.RelayProtocolOptions
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.Validation
import com.walletconnect.android.internal.common.model.WCRequest
import com.walletconnect.android.internal.common.model.WCResponse
import com.walletconnect.android.internal.common.model.params.CoreSignParams
import com.walletconnect.android.internal.common.model.type.ClientParams
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.storage.MetadataStorageRepositoryInterface
import com.walletconnect.android.internal.common.storage.VerifyContextStorageRepository
import com.walletconnect.android.internal.utils.ACTIVE_SESSION
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
import com.walletconnect.sign.common.exceptions.InvalidNamespaceException
import com.walletconnect.sign.common.exceptions.InvalidPropertiesException
import com.walletconnect.sign.common.exceptions.InvalidRequestException
import com.walletconnect.sign.common.exceptions.NO_SEQUENCE_FOR_TOPIC_MESSAGE
import com.walletconnect.sign.common.exceptions.NotSettledSessionException
import com.walletconnect.sign.common.exceptions.PeerError
import com.walletconnect.sign.common.exceptions.SESSION_IS_NOT_ACKNOWLEDGED_MESSAGE
import com.walletconnect.sign.common.exceptions.UNAUTHORIZED_EMIT_MESSAGE
import com.walletconnect.sign.common.exceptions.UNAUTHORIZED_EXTEND_MESSAGE
import com.walletconnect.sign.common.exceptions.UNAUTHORIZED_UPDATE_MESSAGE
import com.walletconnect.sign.common.exceptions.UnauthorizedEventException
import com.walletconnect.sign.common.exceptions.UnauthorizedMethodException
import com.walletconnect.sign.common.exceptions.UnauthorizedPeerException
import com.walletconnect.sign.common.model.PendingRequest
import com.walletconnect.sign.common.model.type.Sequences
import com.walletconnect.sign.common.model.vo.clientsync.common.NamespaceVO
import com.walletconnect.sign.common.model.vo.clientsync.common.SessionParticipantVO
import com.walletconnect.sign.common.model.vo.clientsync.session.SignRpc
import com.walletconnect.sign.common.model.vo.clientsync.session.params.SignParams
import com.walletconnect.sign.common.model.vo.clientsync.session.payload.SessionEventVO
import com.walletconnect.sign.common.model.vo.clientsync.session.payload.SessionRequestVO
import com.walletconnect.sign.common.model.vo.proposal.ProposalVO
import com.walletconnect.sign.common.model.vo.sequence.SessionVO
import com.walletconnect.sign.common.validator.SignValidator
import com.walletconnect.sign.engine.model.EngineDO
import com.walletconnect.sign.engine.model.mapper.toEngineDO
import com.walletconnect.sign.engine.model.mapper.toEngineDOEvent
import com.walletconnect.sign.engine.model.mapper.toEngineDOSessionExtend
import com.walletconnect.sign.engine.model.mapper.toMapOfEngineNamespacesSession
import com.walletconnect.sign.engine.model.mapper.toMapOfNamespacesVOSession
import com.walletconnect.sign.engine.model.mapper.toNamespacesVOOptional
import com.walletconnect.sign.engine.model.mapper.toNamespacesVORequired
import com.walletconnect.sign.engine.model.mapper.toPeerError
import com.walletconnect.sign.engine.model.mapper.toSessionApproveParams
import com.walletconnect.sign.engine.model.mapper.toSessionApproved
import com.walletconnect.sign.engine.model.mapper.toSessionProposeParams
import com.walletconnect.sign.engine.model.mapper.toSessionProposeRequest
import com.walletconnect.sign.engine.model.mapper.toSessionRequest
import com.walletconnect.sign.engine.model.mapper.toSessionSettleParams
import com.walletconnect.sign.engine.model.mapper.toVO
import com.walletconnect.sign.engine.use_case.PairUseCase
import com.walletconnect.sign.engine.use_case.PairUseCaseInterface
import com.walletconnect.sign.engine.use_case.ProposeSessionUseCase
import com.walletconnect.sign.engine.use_case.ProposeSessionUseCaseInterface
import com.walletconnect.sign.json_rpc.domain.GetPendingJsonRpcHistoryEntryByIdUseCase
import com.walletconnect.sign.json_rpc.domain.GetPendingRequestsUseCaseByTopic
import com.walletconnect.sign.json_rpc.domain.GetPendingSessionRequests
import com.walletconnect.sign.json_rpc.model.JsonRpcMethod
import com.walletconnect.sign.storage.proposal.ProposalStorageRepository
import com.walletconnect.sign.storage.sequence.SessionStorageRepository
import com.walletconnect.utils.Empty
import com.walletconnect.utils.extractTimestamp
import com.walletconnect.utils.isSequenceValid
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
import java.util.LinkedList
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

internal class SignEngine(
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val getPendingRequestsByTopicUseCase: GetPendingRequestsUseCaseByTopic,
    private val getPendingSessionRequests: GetPendingSessionRequests,
    private val getPendingJsonRpcHistoryEntryByIdUseCase: GetPendingJsonRpcHistoryEntryByIdUseCase,
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
    private val logger: Logger,
    private val proposeSessionUseCase: ProposeSessionUseCase,
    private val pairUseCase: PairUseCase
) : ProposeSessionUseCaseInterface by proposeSessionUseCase,
    PairUseCaseInterface by pairUseCase {

    private var jsonRpcRequestsJob: Job? = null
    private var jsonRpcResponsesJob: Job? = null
    private var internalErrorsJob: Job? = null
    private val _engineEvent: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val engineEvent: SharedFlow<EngineEvent> = _engineEvent.asSharedFlow()
    private val sessionRequestsQueue: LinkedList<EngineDO.SessionRequestEvent> = LinkedList()

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
            }.launchIn(scope)
    }

    internal fun reject(proposerPublicKey: String, reason: String, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit = {}) {
        val proposal = proposalStorageRepository.getProposalByKey(proposerPublicKey)
        proposalStorageRepository.deleteProposal(proposerPublicKey)
        scope.launch {
            supervisorScope {
                verifyContextStorageRepository.delete(proposal.requestId)
            }
        }

        jsonRpcInteractor.respondWithError(
            proposal.toSessionProposeRequest(),
            PeerError.EIP1193.UserRejectedRequest(reason),
            IrnParams(Tags.SESSION_PROPOSE_RESPONSE, Ttl(FIVE_MINUTES_IN_SECONDS)),
            onSuccess = { onSuccess() },
            onFailure = { error -> onFailure(error) })
    }

    internal fun approve(
        proposerPublicKey: String,
        sessionNamespaces: Map<String, EngineDO.Namespace.Session>,
        onSuccess: () -> Unit = {},
        onFailure: (Throwable) -> Unit = {},
    ) {
        fun sessionSettle(
            requestId: Long,
            proposal: ProposalVO,
            sessionTopic: Topic,
            pairingTopic: Topic,
        ) {
            val selfPublicKey = crypto.getSelfPublicFromKeyAgreement(sessionTopic)
            val selfParticipant = SessionParticipantVO(selfPublicKey.keyAsHex, selfAppMetaData)
            val sessionExpiry = ACTIVE_SESSION
            val unacknowledgedSession =
                SessionVO.createUnacknowledgedSession(sessionTopic, proposal, selfParticipant, sessionExpiry, sessionNamespaces, pairingTopic.value)

            try {
                sessionStorageRepository.insertSession(unacknowledgedSession, requestId)
                metadataStorageRepository.insertOrAbortMetadata(sessionTopic, selfAppMetaData, AppMetaDataType.SELF)
                metadataStorageRepository.insertOrAbortMetadata(sessionTopic, proposal.appMetaData, AppMetaDataType.PEER)
                val params = proposal.toSessionSettleParams(selfParticipant, sessionExpiry, sessionNamespaces)
                val sessionSettle = SignRpc.SessionSettle(params = params)
                val irnParams = IrnParams(Tags.SESSION_SETTLE, Ttl(FIVE_MINUTES_IN_SECONDS))

                jsonRpcInteractor.publishJsonRpcRequest(
                    topic = sessionTopic,
                    params = irnParams, sessionSettle,
                    onSuccess = { onSuccess() },
                    onFailure = { error -> onFailure(error) }
                )
            } catch (e: SQLiteException) {
                sessionStorageRepository.deleteSession(sessionTopic)
                // todo: missing metadata deletion. Also check other try catches
                onFailure(e)
            }
        }

        val proposal = proposalStorageRepository.getProposalByKey(proposerPublicKey)
        proposalStorageRepository.deleteProposal(proposerPublicKey)
        scope.launch {
            supervisorScope {
                verifyContextStorageRepository.delete(proposal.requestId)
            }
        }
        val request = proposal.toSessionProposeRequest()

        SignValidator.validateSessionNamespace(sessionNamespaces.toMapOfNamespacesVOSession(), proposal.requiredNamespaces) { error ->
            throw InvalidNamespaceException(error.message)
        }

        val selfPublicKey: PublicKey = crypto.generateAndStoreX25519KeyPair()
        val sessionTopic = crypto.generateTopicFromKeyAgreement(selfPublicKey, PublicKey(proposerPublicKey))
        val approvalParams = proposal.toSessionApproveParams(selfPublicKey)
        val irnParams = IrnParams(Tags.SESSION_PROPOSE_RESPONSE, Ttl(FIVE_MINUTES_IN_SECONDS))
        jsonRpcInteractor.subscribe(sessionTopic) { error -> throw error }
        jsonRpcInteractor.respondWithParams(request, approvalParams, irnParams) { error -> throw error }

        sessionSettle(request.id, proposal, sessionTopic, request.topic)
    }

    internal fun sessionUpdate(
        topic: String,
        namespaces: Map<String, EngineDO.Namespace.Session>,
        onSuccess: () -> Unit,
        onFailure: (Throwable) -> Unit,
    ) {
        if (!sessionStorageRepository.isSessionValid(Topic(topic))) {
            throw CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE$topic")
        }

        val session = sessionStorageRepository.getSessionWithoutMetadataByTopic(Topic(topic))

        if (!session.isSelfController) {
            throw UnauthorizedPeerException(UNAUTHORIZED_UPDATE_MESSAGE)
        }

        if (!session.isAcknowledged) {
            throw NotSettledSessionException("$SESSION_IS_NOT_ACKNOWLEDGED_MESSAGE$topic")
        }

        SignValidator.validateSessionNamespace(namespaces.toMapOfNamespacesVOSession(), session.requiredNamespaces) { error ->
            throw InvalidNamespaceException(error.message)
        }

        val params = SignParams.UpdateNamespacesParams(namespaces.toMapOfNamespacesVOSession())
        val sessionUpdate = SignRpc.SessionUpdate(params = params)
        val irnParams = IrnParams(Tags.SESSION_UPDATE, Ttl(DAY_IN_SECONDS))

        try {
            sessionStorageRepository.insertTempNamespaces(topic, namespaces.toMapOfNamespacesVOSession(), sessionUpdate.id)
            jsonRpcInteractor.publishJsonRpcRequest(
                Topic(topic), irnParams, sessionUpdate,
                onSuccess = {
                    logger.log("Update sent successfully")
                    onSuccess()
                },
                onFailure = { error ->
                    logger.error("Sending session update error: $error")
                    sessionStorageRepository.deleteTempNamespacesByRequestId(sessionUpdate.id)
                    onFailure(error)
                })
        } catch (e: Exception) {
            onFailure(GenericException("Error updating namespaces: $e"))
        }
    }

    internal fun sessionRequest(request: EngineDO.Request, onSuccess: (Long) -> Unit, onFailure: (Throwable) -> Unit) {
        if (!sessionStorageRepository.isSessionValid(Topic(request.topic))) {
            throw CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE${request.topic}")
        }

        val nowInSeconds = TimeUnit.SECONDS.convert(Date().time, TimeUnit.SECONDS)
        if (!CoreValidator.isExpiryWithinBounds(request.expiry ?: Expiry(300))) {
            throw InvalidExpiryException()
        }

        SignValidator.validateSessionRequest(request) { error ->
            throw InvalidRequestException(error.message)
        }

        val namespaces: Map<String, NamespaceVO.Session> =
            sessionStorageRepository.getSessionWithoutMetadataByTopic(Topic(request.topic)).sessionNamespaces
        SignValidator.validateChainIdWithMethodAuthorisation(request.chainId, request.method, namespaces) { error ->
            throw UnauthorizedMethodException(error.message)
        }

        val params = SignParams.SessionRequestParams(SessionRequestVO(request.method, request.params), request.chainId)
        val sessionPayload = SignRpc.SessionRequest(params = params)
        val irnParamsTtl = request.expiry?.run {
            val defaultTtl = FIVE_MINUTES_IN_SECONDS
            val extractedTtl = seconds - nowInSeconds
            val newTtl = extractedTtl.takeIf { extractedTtl >= defaultTtl } ?: defaultTtl

            Ttl(newTtl)
        } ?: Ttl(FIVE_MINUTES_IN_SECONDS)
        val irnParams = IrnParams(Tags.SESSION_REQUEST, irnParamsTtl, true)
        val requestTtlInSeconds = request.expiry?.run {
            seconds - nowInSeconds
        } ?: FIVE_MINUTES_IN_SECONDS

        jsonRpcInteractor.publishJsonRpcRequest(
            Topic(request.topic),
            irnParams,
            sessionPayload,
            onSuccess = {
                logger.log("Session request sent successfully")
                onSuccess(sessionPayload.id)
                scope.launch {
                    try {
                        withTimeout(TimeUnit.SECONDS.toMillis(requestTtlInSeconds)) {
                            collectResponse(sessionPayload.id) { cancel() }
                        }
                    } catch (e: TimeoutCancellationException) {
                        _engineEvent.emit(SDKError(e))
                    }
                }
            },
            onFailure = { error ->
                logger.error("Sending session request error: $error")
                onFailure(error)
            }
        )
    }

    internal fun respondSessionRequest(
        topic: String,
        jsonRpcResponse: JsonRpcResponse,
        onSuccess: () -> Unit,
        onFailure: (Throwable) -> Unit,
    ) {
        val topicWrapper = Topic(topic)
        if (!sessionStorageRepository.isSessionValid(topicWrapper)) {
            throw CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE$topic")
        }

        getPendingJsonRpcHistoryEntryByIdUseCase(jsonRpcResponse.id)?.params?.request?.expiry?.let { expiry ->
            if (!CoreValidator.isExpiryWithinBounds(expiry)) {
                scope.launch {
                    supervisorScope {
                        val irnParams = IrnParams(Tags.SESSION_REQUEST_RESPONSE, Ttl(FIVE_MINUTES_IN_SECONDS))
                        val request = WCRequest(Topic(topic), jsonRpcResponse.id, JsonRpcMethod.WC_SESSION_REQUEST, object : ClientParams {})
                        jsonRpcInteractor.respondWithError(request, Invalid.RequestExpired, irnParams, onSuccess = {
                            scope.launch {
                                supervisorScope {
                                    removePendingSessionRequestAndEmit(jsonRpcResponse)
                                }
                            }
                        })
                    }
                }

                throw InvalidExpiryException()
            }
        }

        val irnParams = IrnParams(Tags.SESSION_REQUEST_RESPONSE, Ttl(FIVE_MINUTES_IN_SECONDS))

        jsonRpcInteractor.publishJsonRpcResponse(
            topic = Topic(topic),
            params = irnParams,
            response = jsonRpcResponse,
            onSuccess = {
                logger.log("Session payload sent successfully")

                scope.launch {
                    supervisorScope {
                        removePendingSessionRequestAndEmit(jsonRpcResponse)
                    }
                }
                onSuccess()
            },
            onFailure = { error ->
                logger.error("Sending session payload response error: $error")
                onFailure(error)
            }
        )
    }

    internal fun ping(topic: String, onSuccess: (String) -> Unit, onFailure: (Throwable) -> Unit, timeout: Duration = THIRTY_SECONDS_TIMEOUT) {
        if (sessionStorageRepository.isSessionValid(Topic(topic))) {
            val pingPayload = SignRpc.SessionPing(params = SignParams.PingParams())
            val irnParams = IrnParams(Tags.SESSION_PING, Ttl(THIRTY_SECONDS))

            jsonRpcInteractor.publishJsonRpcRequest(Topic(topic), irnParams, pingPayload,
                onSuccess = {
                    logger.log("Ping sent successfully")
                    scope.launch {
                        try {
                            withTimeout(timeout) {
                                collectResponse(pingPayload.id) { result ->
                                    cancel()
                                    result.fold(
                                        onSuccess = {
                                            logger.log("Ping peer response success")
                                            onSuccess(topic)
                                        },
                                        onFailure = { error ->
                                            logger.log("Ping peer response error: $error")
                                            onFailure(error)
                                        })
                                }
                            }
                        } catch (e: TimeoutCancellationException) {
                            onFailure(e)
                        }
                    }
                },
                onFailure = { error ->
                    logger.log("Ping sent error: $error")
                    onFailure(error)
                })
        } else {
            pairingInterface.ping(Core.Params.Ping(topic), object : Core.Listeners.PairingPing {
                override fun onSuccess(pingSuccess: Core.Model.Ping.Success) {
                    onSuccess(pingSuccess.topic)
                }

                override fun onError(pingError: Core.Model.Ping.Error) {
                    onFailure(pingError.error)
                }
            })
        }
    }

    internal fun emit(topic: String, event: EngineDO.Event, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
        if (!sessionStorageRepository.isSessionValid(Topic(topic))) {
            throw CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE$topic")
        }

        val session = sessionStorageRepository.getSessionWithoutMetadataByTopic(Topic(topic))
        if (!session.isSelfController) {
            throw UnauthorizedPeerException(UNAUTHORIZED_EMIT_MESSAGE)
        }

        SignValidator.validateEvent(event) { error ->
            throw InvalidEventException(error.message)
        }

        val namespaces = session.sessionNamespaces
        SignValidator.validateChainIdWithEventAuthorisation(event.chainId, event.name, namespaces) { error ->
            throw UnauthorizedEventException(error.message)
        }

        val eventParams = SignParams.EventParams(SessionEventVO(event.name, event.data), event.chainId)
        val sessionEvent = SignRpc.SessionEvent(params = eventParams)
        val irnParams = IrnParams(Tags.SESSION_EVENT, Ttl(FIVE_MINUTES_IN_SECONDS), true)

        jsonRpcInteractor.publishJsonRpcRequest(Topic(topic), irnParams, sessionEvent,
            onSuccess = {
                logger.log("Event sent successfully")
                onSuccess()
            },
            onFailure = { error ->
                logger.error("Sending event error: $error")
                onFailure(error)
            }
        )
    }

    internal fun extend(topic: String, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
        if (!sessionStorageRepository.isSessionValid(Topic(topic))) {
            throw CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE$topic")
        }

        val session = sessionStorageRepository.getSessionWithoutMetadataByTopic(Topic(topic))
        if (!session.isSelfController) {
            throw UnauthorizedPeerException(UNAUTHORIZED_EXTEND_MESSAGE)
        }
        if (!session.isAcknowledged) {
            throw NotSettledSessionException("$SESSION_IS_NOT_ACKNOWLEDGED_MESSAGE$topic")
        }

        val newExpiration = session.expiry.seconds + WEEK_IN_SECONDS
        sessionStorageRepository.extendSession(Topic(topic), newExpiration)
        val sessionExtend = SignRpc.SessionExtend(params = SignParams.ExtendParams(newExpiration))
        val irnParams = IrnParams(Tags.SESSION_EXTEND, Ttl(DAY_IN_SECONDS))

        jsonRpcInteractor.publishJsonRpcRequest(Topic(topic), irnParams, sessionExtend,
            onSuccess = {
                logger.log("Session extend sent successfully")
                onSuccess()
            },
            onFailure = { error ->
                logger.error("Sending session extend error: $error")
                onFailure(error)
            })
    }

    internal fun disconnect(topic: String, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
        if (!sessionStorageRepository.isSessionValid(Topic(topic))) {
            throw CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE$topic")
        }

        val deleteParams = SignParams.DeleteParams(Reason.UserDisconnected.code, Reason.UserDisconnected.message)
        val sessionDelete = SignRpc.SessionDelete(params = deleteParams)
        sessionStorageRepository.deleteSession(Topic(topic))
        jsonRpcInteractor.unsubscribe(Topic(topic))
        val irnParams = IrnParams(Tags.SESSION_DELETE, Ttl(DAY_IN_SECONDS))

        jsonRpcInteractor.publishJsonRpcRequest(Topic(topic), irnParams, sessionDelete,
            onSuccess = {
                logger.log("Disconnect sent successfully")
                onSuccess()
            },
            onFailure = { error ->
                logger.error("Sending session disconnect error: $error")
                onFailure(error)
            }
        )
    }

    internal fun getListOfSettledSessions(): List<EngineDO.Session> {
        return sessionStorageRepository.getListOfSessionVOsWithoutMetadata()
            .filter { session -> session.isAcknowledged && session.expiry.isSequenceValid() }
            .map { session ->
                val peerMetaData = metadataStorageRepository.getByTopicAndType(session.topic, AppMetaDataType.PEER)
                session.copy(selfAppMetaData = selfAppMetaData, peerAppMetaData = peerMetaData)
            }
            .map { session -> session.toEngineDO() }
    }

    internal fun getListOfSettledPairings(): List<EngineDO.PairingSettle> {
        return pairingInterface.getPairings().map { pairing ->
            val mappedPairing = pairing.toPairing()
            EngineDO.PairingSettle(mappedPairing.topic, mappedPairing.peerAppMetaData)
        }
    }

    internal fun getPendingRequests(topic: Topic): List<PendingRequest<String>> = getPendingRequestsByTopicUseCase(topic)

    internal fun getSessionProposals(): List<EngineDO.SessionProposal> = proposalStorageRepository.getProposals().map(ProposalVO::toEngineDO)

    internal fun getPendingSessionRequests(topic: Topic): List<EngineDO.SessionRequest> = getPendingRequestsByTopicUseCase(topic)
        .map { pendingRequest ->
            val peerMetaData = metadataStorageRepository.getByTopicAndType(pendingRequest.topic, AppMetaDataType.PEER)
            pendingRequest.toSessionRequest(peerMetaData)
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

    private suspend fun removePendingSessionRequestAndEmit(jsonRpcResponse: JsonRpcResponse) {
        verifyContextStorageRepository.delete(jsonRpcResponse.id)
        sessionRequestsQueue.find { pendingRequestEvent -> pendingRequestEvent.request.request.id == jsonRpcResponse.id }?.let { event ->
            sessionRequestsQueue.remove(event)
        }
        if (sessionRequestsQueue.isNotEmpty()) {
            _engineEvent.emit(sessionRequestsQueue.first())
        }
    }

    private suspend fun collectResponse(id: Long, onResponse: (Result<JsonRpcResponse.JsonRpcResult>) -> Unit = {}) {
        jsonRpcInteractor.peerResponse
            .filter { response -> response.response.id == id }
            .collect { response ->
                when (val result = response.response) {
                    is JsonRpcResponse.JsonRpcResult -> onResponse(Result.success(result))
                    is JsonRpcResponse.JsonRpcError -> onResponse(Result.failure(Throwable(result.errorMessage)))
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
        merge(jsonRpcInteractor.internalErrors, pairingController.findWrongMethodsFlow)
            .onEach { exception -> _engineEvent.emit(exception) }
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

    private companion object {
        val THIRTY_SECONDS_TIMEOUT: Duration = 30.seconds
    }
}