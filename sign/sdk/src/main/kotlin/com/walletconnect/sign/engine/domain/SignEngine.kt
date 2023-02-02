@file:JvmSynthetic

package com.walletconnect.sign.engine.domain

import android.database.sqlite.SQLiteException
import com.walletconnect.android.Core
import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.exception.CannotFindSequenceForTopic
import com.walletconnect.android.internal.common.exception.GenericException
import com.walletconnect.android.internal.common.exception.Reason
import com.walletconnect.android.internal.common.exception.Uncategorized
import com.walletconnect.android.internal.common.model.*
import com.walletconnect.android.internal.common.model.params.CoreSignParams
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.storage.MetadataStorageRepositoryInterface
import com.walletconnect.android.internal.utils.*
import com.walletconnect.android.pairing.client.PairingInterface
import com.walletconnect.android.pairing.handler.PairingControllerInterface
import com.walletconnect.android.pairing.model.mapper.toClient
import com.walletconnect.android.pairing.model.mapper.toPairing
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.foundation.util.Logger
import com.walletconnect.sign.common.exceptions.*
import com.walletconnect.sign.common.model.PendingRequest
import com.walletconnect.sign.common.model.type.Sequences
import com.walletconnect.sign.common.model.vo.clientsync.common.NamespaceVO
import com.walletconnect.sign.common.model.vo.clientsync.common.SessionParticipantVO
import com.walletconnect.sign.common.model.vo.clientsync.session.SignRpc
import com.walletconnect.sign.common.model.vo.clientsync.session.params.SignParams
import com.walletconnect.sign.common.model.vo.clientsync.session.payload.SessionEventVO
import com.walletconnect.sign.common.model.vo.clientsync.session.payload.SessionRequestVO
import com.walletconnect.sign.common.model.vo.sequence.SessionVO
import com.walletconnect.sign.engine.model.EngineDO
import com.walletconnect.sign.engine.model.mapper.*
import com.walletconnect.sign.json_rpc.domain.GetPendingRequestsUseCase
import com.walletconnect.sign.json_rpc.model.JsonRpcMethod
import com.walletconnect.sign.storage.sequence.SessionStorageRepository
import com.walletconnect.util.generateId
import com.walletconnect.utils.Empty
import com.walletconnect.utils.extractTimestamp
import com.walletconnect.utils.isSequenceValid
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

internal class SignEngine(
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val getPendingRequestsUseCase: GetPendingRequestsUseCase,
    private val crypto: KeyManagementRepository,
    private val sessionStorageRepository: SessionStorageRepository,
    private val metadataStorageRepository: MetadataStorageRepositoryInterface,
    private val pairingInterface: PairingInterface,
    private val pairingHandler: PairingControllerInterface,
    private val selfAppMetaData: AppMetaData,
    private val logger: Logger
) {
    private var jsonRpcRequestsJob: Job? = null
    private var jsonRpcResponsesJob: Job? = null
    private var internalErrorsJob: Job? = null
    private val _engineEvent: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val engineEvent: SharedFlow<EngineEvent> = _engineEvent.asSharedFlow()
    private val sessionProposalRequest: MutableMap<String, WCRequest> = mutableMapOf()

    init {
        pairingHandler.register(
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

    internal fun proposeSession(
        requiredNamespaces: Map<String, EngineDO.Namespace.Required>?,
        optionalNamespaces: Map<String, EngineDO.Namespace.Optional>?,
        pairing: Pairing,
        onSuccess: () -> Unit,
        onFailure: (Throwable) -> Unit,
    ) {
        val relay = RelayProtocolOptions(pairing.relayProtocol, pairing.relayData)

        requiredNamespaces?.let { namespaces ->
            SignValidator.validateOptionalNamespaces(namespaces.toNamespacesVORequired()) { error ->
                throw InvalidNamespaceException(error.message)
            }
        }

        optionalNamespaces?.let { namespaces ->
            SignValidator.validateOptionalNamespaces(namespaces.toNamespacesVOOptional()) { error ->
                throw InvalidNamespaceException(error.message)
            }
        }

        val selfPublicKey: PublicKey = crypto.generateKeyPair()
        val sessionProposal: SignParams.SessionProposeParams =
            toSessionProposeParams(
                listOf(relay), requiredNamespaces ?: emptyMap(),
                optionalNamespaces ?: emptyMap(),
                selfPublicKey, selfAppMetaData
            )

        val request = SignRpc.SessionPropose(id = generateId(), params = sessionProposal)
        sessionProposalRequest[selfPublicKey.keyAsHex] = WCRequest(pairing.topic, request.id, request.method, sessionProposal)
        val irnParams = IrnParams(Tags.SESSION_PROPOSE, Ttl(FIVE_MINUTES_IN_SECONDS), true)
        jsonRpcInteractor.subscribe(pairing.topic) { error -> return@subscribe onFailure(error) }

        jsonRpcInteractor.publishJsonRpcRequest(pairing.topic, irnParams, request,
            onSuccess = {
                logger.log("Session proposal sent successfully")
                onSuccess()
            },
            onFailure = { error ->
                logger.error("Failed to send a session proposal: $error")
                onFailure(error)
            })
    }

    internal fun pair(uri: String, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
        pairingInterface.pair(
            pair = Core.Params.Pair(uri),
            onSuccess = { onSuccess() },
            onError = { error -> onFailure(error.throwable) }
        )
    }

    internal fun reject(proposerPublicKey: String, reason: String, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit = {}) {
        val request = sessionProposalRequest[proposerPublicKey]
            ?: throw CannotFindSessionProposalException("$NO_SESSION_PROPOSAL$proposerPublicKey")
        sessionProposalRequest.remove(proposerPublicKey)
        val irnParams = IrnParams(Tags.SESSION_PROPOSE_RESPONSE, Ttl(FIVE_MINUTES_IN_SECONDS))

        jsonRpcInteractor.respondWithError(
            request,
            PeerError.EIP1193.UserRejectedRequest(reason),
            irnParams,
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
            proposal: SignParams.SessionProposeParams,
            sessionTopic: Topic,
            pairingTopic: Topic,
        ) {
            val selfPublicKey = crypto.getSelfPublicFromKeyAgreement(sessionTopic)
            val selfParticipant = SessionParticipantVO(selfPublicKey.keyAsHex, selfAppMetaData)
            val sessionExpiry = ACTIVE_SESSION
            val unacknowledgedSession =
                SessionVO.createUnacknowledgedSession(sessionTopic, proposal, selfParticipant, sessionExpiry, sessionNamespaces)

            try {
                sessionStorageRepository.insertSession(unacknowledgedSession, pairingTopic, requestId)
                metadataStorageRepository.insertOrAbortMetadata(sessionTopic, selfAppMetaData, AppMetaDataType.SELF)
                metadataStorageRepository.insertOrAbortMetadata(sessionTopic, proposal.proposer.metadata, AppMetaDataType.PEER)
                val params = proposal.toSessionSettleParams(selfParticipant, sessionExpiry, sessionNamespaces)
                val sessionSettle = SignRpc.SessionSettle(id = generateId(), params = params)
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

        val request = sessionProposalRequest[proposerPublicKey] ?: throw CannotFindSessionProposalException("$NO_SESSION_PROPOSAL$proposerPublicKey")
        sessionProposalRequest.remove(proposerPublicKey)
        val proposal = request.params as SignParams.SessionProposeParams

        SignValidator.validateSessionNamespace(
            sessionNamespaces.toMapOfNamespacesVOSession(),
            proposal.requiredNamespaces,
            proposal.optionalNamespaces
        ) { error ->
            throw InvalidNamespaceException(error.message)
        }

        val selfPublicKey: PublicKey = crypto.generateKeyPair()
        val sessionTopic = crypto.generateTopicFromKeyAgreement(selfPublicKey, PublicKey(proposerPublicKey))
        val approvalParams = proposal.toSessionApproveParams(selfPublicKey)
        val irnParams = IrnParams(Tags.SESSION_PROPOSE_RESPONSE, Ttl(FIVE_MINUTES_IN_SECONDS))
        jsonRpcInteractor.subscribe(sessionTopic) { error -> return@subscribe onFailure(error) }
        jsonRpcInteractor.respondWithParams(request, approvalParams, irnParams) { error -> return@respondWithParams onFailure(error) }

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

        SignValidator.validateSessionNamespace(
            namespaces.toMapOfNamespacesVOSession(),
            session.requiredNamespaces,
            session.optionalNamespaces
        ) { error ->
            throw InvalidNamespaceException(error.message)
        }

        val params = SignParams.UpdateNamespacesParams(namespaces.toMapOfNamespacesVOSession())
        val sessionUpdate = SignRpc.SessionUpdate(id = generateId(), params = params)
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

    internal fun sessionRequest(request: EngineDO.Request, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
        if (!sessionStorageRepository.isSessionValid(Topic(request.topic))) {
            throw CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE${request.topic}")
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
        val sessionPayload = SignRpc.SessionRequest(id = generateId(), params = params)
        val irnParams = IrnParams(Tags.SESSION_REQUEST, Ttl(FIVE_MINUTES_IN_SECONDS), true)

        jsonRpcInteractor.publishJsonRpcRequest(
            Topic(request.topic),
            irnParams,
            sessionPayload,
            onSuccess = {
                logger.log("Session request sent successfully")
                onSuccess()
                scope.launch {
                    try {
                        withTimeout(FIVE_MINUTES_TIMEOUT) {
                            collectResponse(sessionPayload.id) { cancel() }
                        }
                    } catch (e: TimeoutCancellationException) {
                        onFailure(e)
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
        if (!sessionStorageRepository.isSessionValid(Topic(topic))) {
            throw CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE$topic")
        }
        val irnParams = IrnParams(Tags.SESSION_REQUEST_RESPONSE, Ttl(FIVE_MINUTES_IN_SECONDS))

        jsonRpcInteractor.publishJsonRpcResponse(
            topic = Topic(topic),
            params = irnParams,
            response = jsonRpcResponse,
            onSuccess = {
                logger.log("Session payload sent successfully")
                onSuccess()
            },
            onFailure = { error ->
                logger.error("Sending session payload response error: $error")
                onFailure(error)
            }
        )
    }

    // TODO: Do we still want Session Ping
    internal fun ping(topic: String, onSuccess: (String) -> Unit, onFailure: (Throwable) -> Unit) {
        if (sessionStorageRepository.isSessionValid(Topic(topic))) {
            val pingPayload = SignRpc.SessionPing(id = generateId(), params = SignParams.PingParams())
            val irnParams = IrnParams(Tags.SESSION_PING, Ttl(THIRTY_SECONDS))

            jsonRpcInteractor.publishJsonRpcRequest(Topic(topic), irnParams, pingPayload,
                onSuccess = {
                    logger.log("Ping sent successfully")
                    scope.launch {
                        try {
                            withTimeout(THIRTY_SECONDS_TIMEOUT) {
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
        val sessionEvent = SignRpc.SessionEvent(id = generateId(), params = eventParams)
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
        val sessionExtend = SignRpc.SessionExtend(id = generateId(), params = SignParams.ExtendParams(newExpiration))
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
        val sessionDelete = SignRpc.SessionDelete(id = generateId(), params = deleteParams)
        sessionStorageRepository.deleteSession(Topic(topic))
        jsonRpcInteractor.unsubscribe(Topic(topic))
        val irnParams = IrnParams(Tags.SESSION_DELETE, Ttl(DAY_IN_SECONDS))

        jsonRpcInteractor.publishJsonRpcRequest(Topic(topic), irnParams, sessionDelete,
            onSuccess = {
                logger.error("Disconnect sent successfully")
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

    internal fun getPendingRequests(topic: Topic): List<PendingRequest> = getPendingRequestsUseCase(topic)

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
        merge(jsonRpcInteractor.internalErrors, pairingHandler.findWrongMethodsFlow)
            .onEach { exception -> _engineEvent.emit(SDKError(exception)) }
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

            SignValidator.validateOptionalNamespaces(payloadParams.requiredNamespaces) { error ->
                jsonRpcInteractor.respondWithError(request, error.toPeerError(), irnParams)
                return
            }

            SignValidator.validateOptionalNamespaces(payloadParams.optionalNamespaces) { error ->
                jsonRpcInteractor.respondWithError(request, error.toPeerError(), irnParams)
                return
            }

            sessionProposalRequest[payloadParams.proposer.publicKey] = request
            pairingHandler.updateMetadata(
                Core.Params.UpdateMetadata(
                    request.topic.value,
                    payloadParams.proposer.metadata.toClient(),
                    AppMetaDataType.PEER
                )
            )
            scope.launch { _engineEvent.emit(payloadParams.toEngineDO(request.topic)) }
        } catch (e: Exception) {
            jsonRpcInteractor.respondWithError(
                request,
                Uncategorized.GenericError("Cannot handle a session proposal: ${e.message}, topic: ${request.topic}"),
                irnParams
            )
        }
    }

    // listened by DappDelegate
    private fun onSessionSettle(request: WCRequest, settleParams: SignParams.SessionSettleParams) {
        val sessionTopic = request.topic
        val irnParams = IrnParams(Tags.SESSION_SETTLE, Ttl(FIVE_MINUTES_IN_SECONDS))
        val selfPublicKey: PublicKey = try {
            crypto.getSelfPublicFromKeyAgreement(sessionTopic)
        } catch (e: Exception) {
            jsonRpcInteractor.respondWithError(request, PeerError.Failure.SessionSettlementFailed(e.message ?: String.Empty), irnParams)
            return
        }
        val peerMetadata = settleParams.controller.metadata
        val proposal = sessionProposalRequest[selfPublicKey.keyAsHex] ?: return

        if (proposal.params !is SignParams.SessionProposeParams) {
            jsonRpcInteractor.respondWithError(request, PeerError.Failure.SessionSettlementFailed(NAMESPACE_MISSING_PROPOSAL_MESSAGE), irnParams)
            return
        }

        val requiredNamespaces = (proposal.params as SignParams.SessionProposeParams).requiredNamespaces
        val optionalNamespaces = (proposal.params as SignParams.SessionProposeParams).optionalNamespaces
        SignValidator.validateSessionNamespace(settleParams.namespaces, requiredNamespaces, optionalNamespaces) { error ->
            jsonRpcInteractor.respondWithError(request, error.toPeerError(), irnParams)
            return
        }

        val tempProposalRequest = sessionProposalRequest.getValue(selfPublicKey.keyAsHex)

        try {
            val session = SessionVO.createAcknowledgedSession(
                sessionTopic,
                settleParams,
                selfPublicKey,
                selfAppMetaData,
                requiredNamespaces,
                optionalNamespaces
            )

            sessionProposalRequest.remove(selfPublicKey.keyAsHex)
            sessionStorageRepository.insertSession(session, request.topic, request.id)
            pairingHandler.updateMetadata(Core.Params.UpdateMetadata(proposal.topic.value, peerMetadata.toClient(), AppMetaDataType.PEER))
            metadataStorageRepository.insertOrAbortMetadata(sessionTopic, peerMetadata, AppMetaDataType.PEER)

            jsonRpcInteractor.respondWithSuccess(request, IrnParams(Tags.SESSION_SETTLE, Ttl(FIVE_MINUTES_IN_SECONDS)))
            scope.launch { _engineEvent.emit(session.toSessionApproved()) }
        } catch (e: SQLiteException) {
            sessionProposalRequest[selfPublicKey.keyAsHex] = tempProposalRequest
            sessionStorageRepository.deleteSession(sessionTopic)
            jsonRpcInteractor.respondWithError(request, PeerError.Failure.SessionSettlementFailed(e.message ?: String.Empty), irnParams)
            return
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
            return
        }
    }

    // listened by WalletDelegate
    private fun onSessionRequest(request: WCRequest, params: SignParams.SessionRequestParams) {
        val irnParams = IrnParams(Tags.SESSION_REQUEST_RESPONSE, Ttl(FIVE_MINUTES_IN_SECONDS))
        try {
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

            scope.launch { _engineEvent.emit(params.toEngineDO(request, sessionPeerAppMetaData)) }
        } catch (e: Exception) {
            jsonRpcInteractor.respondWithError(
                request,
                Uncategorized.GenericError("Cannot handle a session request: ${e.message}, topic: ${request.topic}"),
                irnParams
            )
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

            SignValidator.validateSessionNamespace(params.namespaces, session.requiredNamespaces, session.optionalNamespaces) { error ->
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
            pairingHandler.updateExpiry(Core.Params.UpdateExpiry(pairingTopic.value, Expiry(MONTH_IN_SECONDS)))
            pairingHandler.activate(Core.Params.Activate(pairingTopic.value))
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
            scope.launch { scope.launch { _engineEvent.emit(EngineDO.SessionUpdateNamespacesResponse.Error("Unable to update the session")) } }
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
                    jsonRpcInteractor.unsubscribe(sessionTopic, onSuccess = {
                        crypto.removeKeys(sessionTopic.value)
                        sessionStorageRepository.deleteSession(sessionTopic)
                    })
                }

            listOfValidSessions
                .onEach { session ->
                    jsonRpcInteractor.subscribe(session.topic) { error ->
                        scope.launch {
                            _engineEvent.emit(SDKError(error))
                        }
                    }
                }
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

            pairingHandler.topicExpiredFlow.onEach { topic ->
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
        const val THIRTY_SECONDS_TIMEOUT: Long = 30000L
        const val FIVE_MINUTES_TIMEOUT: Long = 300000L
    }
}