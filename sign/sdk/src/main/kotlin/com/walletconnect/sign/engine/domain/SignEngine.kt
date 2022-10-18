@file:JvmSynthetic

package com.walletconnect.sign.engine.domain

import android.database.sqlite.SQLiteException
import com.walletconnect.android.Core
import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.crypto.KeyManagementRepository
import com.walletconnect.android.internal.common.exception.WalletConnectException
import com.walletconnect.android.internal.common.exception.GenericException
import com.walletconnect.android.impl.common.SDKError
import com.walletconnect.android.impl.common.model.ConnectionState
import com.walletconnect.android.impl.common.model.type.EngineEvent
import com.walletconnect.android.impl.common.scope.scope
import com.walletconnect.android.impl.storage.MetadataStorageRepository
import com.walletconnect.android.impl.utils.*
import com.walletconnect.android.internal.common.model.*
import com.walletconnect.android.pairing.Uncategorized
import com.walletconnect.android.pairing.PairingInterface
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.sign.common.exceptions.*
import com.walletconnect.sign.common.exceptions.PeerError
import com.walletconnect.sign.common.model.PendingRequest
import com.walletconnect.sign.common.model.type.Sequences
import com.walletconnect.sign.common.model.vo.clientsync.common.NamespaceVO
import com.walletconnect.sign.common.model.vo.clientsync.common.SessionParticipantVO
import com.walletconnect.sign.common.model.vo.clientsync.session.SessionRpcVO
import com.walletconnect.sign.common.model.vo.clientsync.session.params.SessionParamsVO
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
    private val relayer: JsonRpcInteractorInterface,
    private val getPendingRequestsUseCase: GetPendingRequestsUseCase,
    private val crypto: KeyManagementRepository,
    private val sessionStorageRepository: SessionStorageRepository,
    private val metadataStorageRepository: MetadataStorageRepository,
    private val pairingInterface: PairingInterface,
    private val selfAppMetaData: AppMetaData,
) {
    private val _engineEvent: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val engineEvent: SharedFlow<EngineEvent> = _engineEvent.asSharedFlow()
    private val sessionProposalRequest: MutableMap<String, WCRequest> = mutableMapOf()

    init {
        resubscribeToSequences()
        setupSequenceExpiration()
        collectJsonRpcRequests()
        collectJsonRpcResponses()
        collectInternalErrors()

        pairingInterface.register(
            JsonRpcMethod.WC_SESSION_PROPOSE,
            JsonRpcMethod.WC_SESSION_SETTLE,
            JsonRpcMethod.WC_SESSION_REQUEST,
            JsonRpcMethod.WC_SESSION_EVENT,
            JsonRpcMethod.WC_SESSION_DELETE,
            JsonRpcMethod.WC_SESSION_EXTEND,
            JsonRpcMethod.WC_SESSION_PING,
            JsonRpcMethod.WC_SESSION_UPDATE
        )
    }

    fun handleInitializationErrors(onError: (WalletConnectException) -> Unit) {
        relayer.initializationErrorsFlow.onEach { walletConnectException ->
            onError(walletConnectException)
        }.launchIn(scope)
    }

    private fun resubscribeToSequences() {
        relayer.isConnectionAvailable
            .onEach { isAvailable -> _engineEvent.emit(ConnectionState(isAvailable)) }
            .filter { isAvailable: Boolean -> isAvailable }
            .onEach {
                coroutineScope {
                    launch(Dispatchers.IO) { resubscribeToSession() }
                }
            }
            .launchIn(scope)
    }

    internal fun proposeSequence(
        namespaces: Map<String, EngineDO.Namespace.Proposal>,
        relays: List<RelayProtocolOptions>?,
        pairingTopic: String?,
        onProposedSequence: (Sequence) -> Unit,
        onFailure: (Throwable) -> Unit,
    ) {
        fun proposeSession(
            pairingTopic: Topic,
            proposedRelays: List<RelayProtocolOptions>?,
            proposedSequence: Sequence,
        ) {
            Validator.validateProposalNamespace(namespaces.toNamespacesVOProposal()) { error ->
                throw InvalidNamespaceException(error.message)
            }

            val selfPublicKey: PublicKey = crypto.generateKeyPair()
            val sessionProposal = toSessionProposeParams(proposedRelays ?: relays, namespaces, selfPublicKey, selfAppMetaData)
            val request = SessionRpcVO.SessionPropose(id = generateId(), params = sessionProposal)
            sessionProposalRequest[selfPublicKey.keyAsHex] = WCRequest(pairingTopic, request.id, request.method, sessionProposal)
            val irnParams = IrnParams(Tags.SESSION_PROPOSE, Ttl(FIVE_MINUTES_IN_SECONDS), true)
            relayer.subscribe(pairingTopic)

            relayer.publishJsonRpcRequests(pairingTopic, irnParams, request,
                onSuccess = {
                    Logger.log("Session proposal sent successfully")
                    onProposedSequence(proposedSequence)
                },
                onFailure = { error ->
                    Logger.error("Failed to send a session proposal: $error")
                    onFailure(error)
                })
        }

        if (pairingTopic != null) {
            val pairing = pairingInterface.getPairings().firstOrNull { pairing ->
                pairing.topic.value == pairingTopic
            } ?: return onFailure(CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE$pairingTopic"))
            val relay = RelayProtocolOptions(pairing.relayProtocol, pairing.relayData)

            proposeSession(Topic(pairingTopic), listOf(relay), EngineDO.ProposedSequence.Session)
        } else {
            pairingInterface.create().fold(
                onSuccess = { pairing ->
                    proposeSession(pairing.topic, listOf(RelayProtocolOptions(pairing.relayProtocol, pairing.relayData)), pairing)
                }, onFailure = {
                    onFailure(it)
                })
        }
    }

    internal fun pair(uri: String) {
        pairingInterface.pair(Core.Params.Pair(uri)) {
//            throw MalformedWalletConnectUri(MALFORMED_PAIRING_URI_MESSAGE)
        }
    }

    internal fun reject(proposerPublicKey: String, reason: String, onFailure: (Throwable) -> Unit = {}) {
        val request = sessionProposalRequest[proposerPublicKey]
            ?: throw CannotFindSessionProposalException("$NO_SESSION_PROPOSAL$proposerPublicKey")
        sessionProposalRequest.remove(proposerPublicKey)
        val irnParams = IrnParams(Tags.SESSION_PROPOSE_RESPONSE, Ttl(FIVE_MINUTES_IN_SECONDS))

        relayer.respondWithError(request, PeerError.EIP1193.UserRejectedRequest(reason), irnParams, onFailure = { error -> onFailure(error) })
    }

    internal fun approve(
        proposerPublicKey: String,
        namespaces: Map<String, EngineDO.Namespace.Session>,
        onFailure: (Throwable) -> Unit = {},
    ) {
        fun sessionSettle(
            requestId: Long,
            proposal: SessionParamsVO.SessionProposeParams,
            sessionTopic: Topic,
            pairingTopic: Topic,
        ) {
            val (selfPublicKey, _) = crypto.getKeyAgreement(sessionTopic)
            val selfParticipant = SessionParticipantVO(selfPublicKey.keyAsHex, selfAppMetaData)
            val sessionExpiry = ACTIVE_SESSION
            val unacknowledgedSession = SessionVO.createUnacknowledgedSession(sessionTopic, proposal, selfParticipant, sessionExpiry, namespaces)

            try {
                val peerAppMetaData = with(proposal.proposer.metadata) { AppMetaData(name, description, url, icons, redirect) }
                sessionStorageRepository.insertSession(unacknowledgedSession, pairingTopic, requestId)
                pairingInterface.updateMetadata(pairingTopic.value, peerAppMetaData, AppMetaDataType.PEER) //todo: take care of multiple metadata structures
                val params = proposal.toSessionSettleParams(selfParticipant, sessionExpiry, namespaces)
                val sessionSettle = SessionRpcVO.SessionSettle(id = generateId(), params = params)
                val irnParams = IrnParams(Tags.SESSION_SETTLE, Ttl(FIVE_MINUTES_IN_SECONDS))

                relayer.publishJsonRpcRequests(sessionTopic, irnParams, sessionSettle, onFailure = { error -> onFailure(error) })
            } catch (e: SQLiteException) {
                sessionStorageRepository.deleteSession(sessionTopic)
                onFailure(e)
            }
        }

        val request = sessionProposalRequest[proposerPublicKey] ?: throw CannotFindSessionProposalException("$NO_SESSION_PROPOSAL$proposerPublicKey")
        val proposal = request.params as SessionParamsVO.SessionProposeParams

        Validator.validateSessionNamespace(namespaces.toMapOfNamespacesVOSession(), proposal.namespaces) { error ->
            throw InvalidNamespaceException(error.message)
        }

        val selfPublicKey: PublicKey = crypto.generateKeyPair()
        val sessionTopic = crypto.generateTopicFromKeyAgreement(selfPublicKey, PublicKey(proposerPublicKey))
        val approvalParams = proposal.toSessionApproveParams(selfPublicKey)
        val irnParams = IrnParams(Tags.SESSION_PROPOSE_RESPONSE, Ttl(FIVE_MINUTES_IN_SECONDS))

        relayer.subscribe(sessionTopic)
        relayer.respondWithParams(request, approvalParams, irnParams)

        sessionSettle(request.id, proposal, sessionTopic, request.topic)
    }

    internal fun sessionUpdate(
        topic: String,
        namespaces: Map<String, EngineDO.Namespace.Session>,
        onFailure: (Throwable) -> Unit,
    ) {
        if (!sessionStorageRepository.isSessionValid(Topic(topic))) {
            throw CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE$topic")
        }

        val session = sessionStorageRepository.getSessionByTopic(Topic(topic))

        if (!session.isSelfController) {
            throw UnauthorizedPeerException(UNAUTHORIZED_UPDATE_MESSAGE)
        }

        if (!session.isAcknowledged) {
            throw NotSettledSessionException("$SESSION_IS_NOT_ACKNOWLEDGED_MESSAGE$topic")
        }

        Validator.validateSessionNamespace(namespaces.toMapOfNamespacesVOSession(), session.proposalNamespaces) { error ->
            throw InvalidNamespaceException(error.message)
        }

        val params = SessionParamsVO.UpdateNamespacesParams(namespaces.toMapOfNamespacesVOSession())
        val sessionUpdate = SessionRpcVO.SessionUpdate(id = generateId(), params = params)
        val irnParams = IrnParams(Tags.SESSION_UPDATE, Ttl(DAY_IN_SECONDS))

        sessionStorageRepository.insertTempNamespaces(topic, namespaces.toMapOfNamespacesVOSession(), sessionUpdate.id,
            onSuccess = {
            relayer.publishJsonRpcRequests(Topic(topic), irnParams, sessionUpdate,
                onSuccess = { Logger.log("Update sent successfully") },
                onFailure = { error ->
                    Logger.error("Sending session update error: $error")
                    sessionStorageRepository.deleteTempNamespacesByRequestId(sessionUpdate.id)
                    onFailure(error)
                }
            )
        }, onFailure = {
            onFailure(GenericException("Error updating namespaces"))
        })
    }

    internal fun sessionRequest(request: EngineDO.Request, onFailure: (Throwable) -> Unit) {
        if (!sessionStorageRepository.isSessionValid(Topic(request.topic))) {
            throw CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE${request.topic}")
        }

        Validator.validateSessionRequest(request) { error ->
            throw InvalidRequestException(error.message)
        }

        val namespaces: Map<String, NamespaceVO.Session> = sessionStorageRepository.getSessionByTopic(Topic(request.topic)).namespaces
        Validator.validateChainIdWithMethodAuthorisation(request.chainId, request.method, namespaces) { error ->
            throw UnauthorizedMethodException(error.message)
        }

        val params = SessionParamsVO.SessionRequestParams(SessionRequestVO(request.method, request.params), request.chainId)
        val sessionPayload = SessionRpcVO.SessionRequest(id = generateId(), params = params)
        val irnParams = IrnParams(Tags.SESSION_REQUEST, Ttl(FIVE_MINUTES_IN_SECONDS), true)

        relayer.publishJsonRpcRequests(
            Topic(request.topic),
            irnParams,
            sessionPayload,
            onSuccess = {
                Logger.log("Session request sent successfully")
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
                Logger.error("Sending session request error: $error")
                onFailure(error)
            }
        )
    }

    internal fun respondSessionRequest(
        topic: String,
        jsonRpcResponse: JsonRpcResponse,
        onFailure: (Throwable) -> Unit,
    ) {
        if (!sessionStorageRepository.isSessionValid(Topic(topic))) {
            throw CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE$topic")
        }
        val irnParams = IrnParams(Tags.SESSION_REQUEST_RESPONSE, Ttl(FIVE_MINUTES_IN_SECONDS))

        relayer.publishJsonRpcResponse(Topic(topic), irnParams, jsonRpcResponse,
            { Logger.log("Session payload sent successfully") },
            { error ->
                Logger.error("Sending session payload response error: $error")
                onFailure(error)
            })
    }

    // TODO: Do we still want Session Ping
    internal fun ping(topic: String, onSuccess: (String) -> Unit, onFailure: (Throwable) -> Unit) {
        if (sessionStorageRepository.isSessionValid(Topic(topic))) {
            val pingPayload = SessionRpcVO.SessionPing(id = generateId(), params = SessionParamsVO.PingParams())
            val irnParams = IrnParams(Tags.SESSION_PING, Ttl(THIRTY_SECONDS))

            relayer.publishJsonRpcRequests(Topic(topic), irnParams, pingPayload,
            onSuccess = {
                Logger.log("Ping sent successfully")
                scope.launch {
                    try {
                        withTimeout(THIRTY_SECONDS_TIMEOUT) {
                            collectResponse(pingPayload.id) { result ->
                                cancel()
                                result.fold(
                                    onSuccess = {
                                        Logger.log("Ping peer response success")
                                        onSuccess(topic)
                                    },
                                    onFailure = { error ->

                                        Logger.log("Ping peer response error: $error")

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
                Logger.log("Ping sent error: $error")
                onFailure(error)
            })
        } else {
            pairingInterface.ping(Core.Params.Ping(topic), object : Core.Listeners.SessionPing {
                override fun onSuccess(pingSuccess: Core.Model.Ping.Success) {
                    onSuccess(pingSuccess.topic)
                }

                override fun onError(pingError: Core.Model.Ping.Error) {
                    onFailure(pingError.error)
                }
            })
        }
    }

    internal fun emit(topic: String, event: EngineDO.Event, onFailure: (Throwable) -> Unit) {
        if (!sessionStorageRepository.isSessionValid(Topic(topic))) {
            throw CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE$topic")
        }

        val session = sessionStorageRepository.getSessionByTopic(Topic(topic))
        if (!session.isSelfController) {
            throw UnauthorizedPeerException(UNAUTHORIZED_EMIT_MESSAGE)
        }

        Validator.validateEvent(event) { error ->
            throw InvalidEventException(error.message)
        }

        val namespaces = session.namespaces
        Validator.validateChainIdWithEventAuthorisation(event.chainId, event.name, namespaces) { error ->
            throw UnauthorizedEventException(error.message)
        }

        val eventParams = SessionParamsVO.EventParams(SessionEventVO(event.name, event.data), event.chainId)
        val sessionEvent = SessionRpcVO.SessionEvent(id = generateId(), params = eventParams)
        val irnParams = IrnParams(Tags.SESSION_EVENT, Ttl(FIVE_MINUTES_IN_SECONDS), true)

        relayer.publishJsonRpcRequests(Topic(topic), irnParams, sessionEvent,
            onSuccess = { Logger.log("Event sent successfully") },
            onFailure = { error ->
                Logger.error("Sending event error: $error")
                onFailure(error)
            }
        )
    }

    internal fun extend(topic: String, onFailure: (Throwable) -> Unit) {
        if (!sessionStorageRepository.isSessionValid(Topic(topic))) {
            throw CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE$topic")
        }

        val session = sessionStorageRepository.getSessionByTopic(Topic(topic))
        if (!session.isSelfController) {
            throw UnauthorizedPeerException(UNAUTHORIZED_EXTEND_MESSAGE)
        }
        if (!session.isAcknowledged) {
            throw NotSettledSessionException("$SESSION_IS_NOT_ACKNOWLEDGED_MESSAGE$topic")
        }

        val newExpiration = session.expiry.seconds + WEEK_IN_SECONDS
        sessionStorageRepository.extendSession(Topic(topic), newExpiration)
        val sessionExtend = SessionRpcVO.SessionExtend(id = generateId(), params = SessionParamsVO.ExtendParams(newExpiration))
        val irnParams = IrnParams(Tags.SESSION_EXTEND, Ttl(DAY_IN_SECONDS))

        relayer.publishJsonRpcRequests(Topic(topic), irnParams, sessionExtend,
            onSuccess = { Logger.log("Session extend sent successfully") },
            onFailure = { error ->
                Logger.error("Sending session extend error: $error")
                onFailure(error)
            })
    }

    internal fun disconnect(topic: String) {
        if (!sessionStorageRepository.isSessionValid(Topic(topic))) {
            throw CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE$topic")
        }

        val deleteParams = SessionParamsVO.DeleteParams(PeerError.Reason.UserDisconnected.code, PeerError.Reason.UserDisconnected.message)
        val sessionDelete = SessionRpcVO.SessionDelete(id = generateId(), params = deleteParams)
        sessionStorageRepository.deleteSession(Topic(topic))
        relayer.unsubscribe(Topic(topic))
        val irnParams = IrnParams(Tags.SESSION_DELETE, Ttl(DAY_IN_SECONDS))

        relayer.publishJsonRpcRequests(Topic(topic), irnParams, sessionDelete,
            onSuccess = { Logger.error("Disconnect sent successfully") },
            onFailure = { error -> Logger.error("Sending session disconnect error: $error") }
        )
    }

    internal fun getListOfSettledSessions(): List<EngineDO.Session> {
        return sessionStorageRepository.getListOfSessionVOs()
            .filter { session -> session.isAcknowledged && session.expiry.isSequenceValid() }
            .map { session ->
                val peerMetaData = metadataStorageRepository.getByTopicAndType(session.topic, AppMetaDataType.PEER)
                session.copy(selfAppMetaData = selfAppMetaData, peerAppMetaData = peerMetaData)
            }
            .map { session -> session.toEngineDO() }
    }

    internal fun getListOfSettledPairings(): List<EngineDO.PairingSettle> {
        return pairingInterface.getPairings().map { pairing ->
            EngineDO.PairingSettle(pairing.topic, pairing.peerAppMetaData)
        }
    }

    internal fun getPendingRequests(topic: Topic): List<PendingRequest> = getPendingRequestsUseCase(topic)

    private suspend fun collectResponse(id: Long, onResponse: (Result<JsonRpcResponse.JsonRpcResult>) -> Unit = {}) {
        relayer.peerResponse
            .filter { response -> response.response.id == id }
            .collect { response ->
                when (val result = response.response) {
                    is JsonRpcResponse.JsonRpcResult -> onResponse(Result.success(result))
                    is JsonRpcResponse.JsonRpcError -> onResponse(Result.failure(Throwable(result.errorMessage)))
                }
            }
    }

    private fun collectJsonRpcRequests() {
        relayer.clientSyncJsonRpc
            .filter { request -> request.params is SessionParamsVO }
            .onEach { request ->
                when (val requestParams = request.params) {
                    is SessionParamsVO.SessionProposeParams -> onSessionPropose(request, requestParams)
                    is SessionParamsVO.SessionSettleParams -> onSessionSettle(request, requestParams)
                    is SessionParamsVO.SessionRequestParams -> onSessionRequest(request, requestParams)
                    is SessionParamsVO.DeleteParams -> onSessionDelete(request, requestParams)
                    is SessionParamsVO.EventParams -> onSessionEvent(request, requestParams)
                    is SessionParamsVO.UpdateNamespacesParams -> onSessionUpdate(request, requestParams)
                    is SessionParamsVO.ExtendParams -> onSessionExtend(request, requestParams)
                    is SessionParamsVO.PingParams -> onPing(request)
                }
            }.launchIn(scope)
    }

    private fun collectInternalErrors() {
        relayer.internalErrors
            .onEach { exception -> _engineEvent.emit(SDKError(exception)) }
            .launchIn(scope)
    }

    private fun collectJsonRpcResponses() {
        scope.launch {
            relayer.peerResponse.collect { response ->
                when (val params = response.params) {
                    is SessionParamsVO.SessionProposeParams -> onSessionProposalResponse(response, params)
                    is SessionParamsVO.SessionSettleParams -> onSessionSettleResponse(response)
                    is SessionParamsVO.UpdateNamespacesParams -> onSessionUpdateResponse(response)
                    is SessionParamsVO.SessionRequestParams -> onSessionRequestResponse(response, params)
                }
            }
        }
    }

    // listened by WalletDelegate
    private fun onSessionPropose(request: WCRequest, payloadParams: SessionParamsVO.SessionProposeParams) {
        Validator.validateProposalNamespace(payloadParams.namespaces) { error ->
            val irnParams = IrnParams(Tags.SESSION_PROPOSE_RESPONSE, Ttl(FIVE_MINUTES_IN_SECONDS))
            relayer.respondWithError(request, error.toPeerError(), irnParams)
            return
        }

        sessionProposalRequest[payloadParams.proposer.publicKey] = request
        scope.launch { _engineEvent.emit(payloadParams.toEngineDO()) }
    }

    // listened by DappDelegate
    private fun onSessionSettle(request: WCRequest, settleParams: SessionParamsVO.SessionSettleParams) {
        val sessionTopic = request.topic
        val (selfPublicKey, _) = crypto.getKeyAgreement(sessionTopic)
        val peerMetadata = settleParams.controller.metadata
        val proposal = sessionProposalRequest[selfPublicKey.keyAsHex] ?: return
        val irnParams = IrnParams(Tags.SESSION_SETTLE, Ttl(FIVE_MINUTES_IN_SECONDS))

        if (proposal.params !is SessionParamsVO.SessionProposeParams) {
            relayer.respondWithError(request, PeerError.Failure.SessionSettlementFailed(NAMESPACE_MISSING_PROPOSAL_MESSAGE), irnParams)
            return
        }

        val proposalNamespaces = (proposal.params as SessionParamsVO.SessionProposeParams).namespaces

        Validator.validateSessionNamespace(settleParams.namespaces, proposalNamespaces) { error ->
            relayer.respondWithError(request, error.toPeerError(), irnParams)
            return
        }

        val tempProposalRequest = sessionProposalRequest.getValue(selfPublicKey.keyAsHex)

        try {
            val session = SessionVO.createAcknowledgedSession(sessionTopic, settleParams, selfPublicKey, selfAppMetaData, proposalNamespaces)

            sessionProposalRequest.remove(selfPublicKey.keyAsHex)
            sessionStorageRepository.insertSession(session, request.topic, request.id)
            metadataStorageRepository.upsertPairingPeerMetadata(sessionTopic, peerMetadata, AppMetaDataType.PEER)

            relayer.respondWithSuccess(request, IrnParams(Tags.SESSION_SETTLE, Ttl(FIVE_MINUTES_IN_SECONDS)))
            scope.launch { _engineEvent.emit(session.toSessionApproved()) }
        } catch (e: SQLiteException) {
            sessionProposalRequest[selfPublicKey.keyAsHex] = tempProposalRequest
            sessionStorageRepository.deleteSession(sessionTopic)
            relayer.respondWithError(request, PeerError.Failure.SessionSettlementFailed(e.message ?: String.Empty), irnParams)
            return
        }
    }

    // listened by WalletDelegate
    private fun onSessionDelete(request: WCRequest, params: SessionParamsVO.DeleteParams) {
        if (!sessionStorageRepository.isSessionValid(request.topic)) {
            val irnParams = IrnParams(Tags.SESSION_DELETE_RESPONSE, Ttl(DAY_IN_SECONDS))
            relayer.respondWithError(request, Uncategorized.NoMatchingTopic(Sequences.SESSION.name, request.topic.value), irnParams)
            return
        }

        crypto.removeKeys(request.topic.value)
        sessionStorageRepository.deleteSession(request.topic)
        relayer.unsubscribe(request.topic)

        scope.launch { _engineEvent.emit(params.toEngineDO(request.topic)) }
    }

    // listened by WalletDelegate
    private fun onSessionRequest(request: WCRequest, params: SessionParamsVO.SessionRequestParams) {
        val irnParams = IrnParams(Tags.SESSION_REQUEST_RESPONSE, Ttl(FIVE_MINUTES_IN_SECONDS))
        Validator.validateSessionRequest(params.toEngineDO(request.topic)) { error ->
            relayer.respondWithError(request, error.toPeerError(), irnParams)
            return
        }

        if (!sessionStorageRepository.isSessionValid(request.topic)) {
            relayer.respondWithError(request, Uncategorized.NoMatchingTopic(Sequences.SESSION.name, request.topic.value), irnParams)
            return
        }

        val (sessionNamespaces: Map<String, NamespaceVO.Session>, sessionPeerAppMetaData: AppMetaData?) = sessionStorageRepository.getSessionByTopic(request.topic).run {
            val peerAppMetaData = metadataStorageRepository.getByTopicAndType(this.topic, AppMetaDataType.PEER)
            this.namespaces to peerAppMetaData
        }

        val method = params.request.method
        Validator.validateChainIdWithMethodAuthorisation(params.chainId, method, sessionNamespaces) { error ->
            relayer.respondWithError(request, error.toPeerError(), irnParams)
            return
        }

        scope.launch { _engineEvent.emit(params.toEngineDO(request, sessionPeerAppMetaData)) }
    }

    // listened by DappDelegate
    private fun onSessionEvent(request: WCRequest, params: SessionParamsVO.EventParams) {
        val irnParams = IrnParams(Tags.SESSION_EVENT_RESPONSE, Ttl(FIVE_MINUTES_IN_SECONDS))
        Validator.validateEvent(params.toEngineDOEvent()) { error ->
            relayer.respondWithError(request, error.toPeerError(), irnParams)
            return
        }

        if (!sessionStorageRepository.isSessionValid(request.topic)) {
            relayer.respondWithError(request, Uncategorized.NoMatchingTopic(Sequences.SESSION.name, request.topic.value), irnParams)
            return
        }

        val session = sessionStorageRepository.getSessionByTopic(request.topic)
        if (!session.isPeerController) {
            relayer.respondWithError(request, PeerError.Unauthorized.Event(Sequences.SESSION.name), irnParams)
            return
        }
        if (!session.isAcknowledged) {
            relayer.respondWithError(request, Uncategorized.NoMatchingTopic(Sequences.SESSION.name, request.topic.value), irnParams)
            return
        }

        val event = params.event
        Validator.validateChainIdWithEventAuthorisation(params.chainId, event.name, session.namespaces) { error ->
            relayer.respondWithError(request, error.toPeerError(), irnParams)
            return
        }

        relayer.respondWithSuccess(request, irnParams)
        scope.launch { _engineEvent.emit(params.toEngineDO(request.topic)) }
    }

    // listened by DappDelegate
    private fun onSessionUpdate(request: WCRequest, params: SessionParamsVO.UpdateNamespacesParams) {
        val irnParams = IrnParams(Tags.SESSION_UPDATE_RESPONSE, Ttl(DAY_IN_SECONDS))
        if (!sessionStorageRepository.isSessionValid(request.topic)) {
            relayer.respondWithError(request, Uncategorized.NoMatchingTopic(Sequences.SESSION.name, request.topic.value), irnParams)
            return
        }

        val session: SessionVO = sessionStorageRepository.getSessionByTopic(request.topic)
        if (!session.isPeerController) {
            relayer.respondWithError(request, PeerError.Unauthorized.UpdateRequest(Sequences.SESSION.name), irnParams)
            return
        }

        Validator.validateSessionNamespace(params.namespaces, session.proposalNamespaces) { error ->
            relayer.respondWithError(request, PeerError.Invalid.UpdateRequest(error.message), irnParams)
            return
        }

        if (!sessionStorageRepository.isUpdatedNamespaceValid(session.topic.value, request.id.extractTimestamp())) {
            relayer.respondWithError(request, PeerError.Invalid.UpdateRequest("Update Namespace Request ID too old"), irnParams)
            return
        }

        sessionStorageRepository.deleteNamespaceAndInsertNewNamespace(session.topic.value, params.namespaces, request.id, onSuccess = {
            relayer.respondWithSuccess(request, irnParams)

            scope.launch {
                _engineEvent.emit(EngineDO.SessionUpdateNamespaces(request.topic, params.namespaces.toMapOfEngineNamespacesSession()))
            }
        }, onFailure = {
            relayer.respondWithError(
                request,
                PeerError.Invalid.UpdateRequest("Updating Namespace Failed. Review Namespace structure"),
                irnParams
            )
        })
    }

    // listened by DappDelegate
    private fun onSessionExtend(request: WCRequest, requestParams: SessionParamsVO.ExtendParams) {
        val irnParams = IrnParams(Tags.SESSION_EXTEND_RESPONSE, Ttl(DAY_IN_SECONDS))
        if (!sessionStorageRepository.isSessionValid(request.topic)) {
            relayer.respondWithError(request, Uncategorized.NoMatchingTopic(Sequences.SESSION.name, request.topic.value), irnParams)
            return
        }

        val session = sessionStorageRepository.getSessionByTopic(request.topic)
        if (!session.isPeerController) {
            relayer.respondWithError(request, PeerError.Unauthorized.ExtendRequest(Sequences.SESSION.name), irnParams)
            return
        }

        val newExpiry = requestParams.expiry
        Validator.validateSessionExtend(newExpiry, session.expiry.seconds) { error ->
            relayer.respondWithError(request, error.toPeerError(), irnParams)
            return
        }

        sessionStorageRepository.extendSession(request.topic, newExpiry)
        relayer.respondWithSuccess(request, irnParams)
        scope.launch { _engineEvent.emit(session.toEngineDOSessionExtend(Expiry(newExpiry))) }
    }

    private fun onPing(request: WCRequest) {
        val irnParams = IrnParams(Tags.SESSION_PING_RESPONSE, Ttl(THIRTY_SECONDS))
        relayer.respondWithSuccess(request, irnParams)
    }

    // listened by DappDelegate
    private fun onSessionProposalResponse(wcResponse: WCResponse, params: SessionParamsVO.SessionProposeParams) {
        val pairingTopic = wcResponse.topic

        pairingInterface.updateExpiry(pairingTopic.value, Expiry(MONTH_IN_SECONDS))
        pairingInterface.updateMetadata(pairingTopic.value, params.proposer.metadata, AppMetaDataType.PEER)
        pairingInterface.activate(pairingTopic.value)

        if (!pairingInterface.getPairings().any { pairing -> pairing.topic == pairingTopic }) return

        when (val response = wcResponse.response) {
            is JsonRpcResponse.JsonRpcResult -> {
                Logger.log("Session proposal approve received")
                val selfPublicKey = PublicKey(params.proposer.publicKey)
                val approveParams = response.result as SessionParamsVO.ApprovalParams
                val responderPublicKey = PublicKey(approveParams.responderPublicKey)
                val sessionTopic = crypto.generateTopicFromKeyAgreement(selfPublicKey, responderPublicKey)
                relayer.subscribe(sessionTopic)
            }
            is JsonRpcResponse.JsonRpcError -> {
                Logger.log("Session proposal reject received: ${response.error}")
                scope.launch { _engineEvent.emit(EngineDO.SessionRejected(pairingTopic.value, response.errorMessage)) }
            }
        }
    }

    // listened by WalletDelegate
    private fun onSessionSettleResponse(wcResponse: WCResponse) {
        val sessionTopic = wcResponse.topic
        if (!sessionStorageRepository.isSessionValid(sessionTopic)) return
        val session = sessionStorageRepository.getSessionByTopic(sessionTopic).run {
            val peerAppMetaData = metadataStorageRepository.getByTopicAndType(this.topic, AppMetaDataType.PEER)
            this.copy(selfAppMetaData = selfAppMetaData, peerAppMetaData = peerAppMetaData)
        }

        when (wcResponse.response) {
            is JsonRpcResponse.JsonRpcResult -> {
                Logger.log("Session settle success received")
                sessionStorageRepository.acknowledgeSession(sessionTopic)
                scope.launch { _engineEvent.emit(EngineDO.SettledSessionResponse.Result(session.toEngineDO())) }
            }
            is JsonRpcResponse.JsonRpcError -> {
                Logger.error("Peer failed to settle session: ${(wcResponse.response as JsonRpcResponse.JsonRpcError).errorMessage}")
                relayer.unsubscribe(sessionTopic)
                sessionStorageRepository.deleteSession(sessionTopic)
                crypto.removeKeys(sessionTopic.value)
            }
        }
    }

    // listened by WalletDelegate
    private fun onSessionUpdateResponse(wcResponse: WCResponse) {
        val sessionTopic = wcResponse.topic
        if (!sessionStorageRepository.isSessionValid(sessionTopic)) return
        val session = sessionStorageRepository.getSessionByTopic(sessionTopic)
        if (!sessionStorageRepository.isUpdatedNamespaceResponseValid(session.topic.value, wcResponse.response.id.extractTimestamp())) {
            return
        }

        when (val response = wcResponse.response) {
            is JsonRpcResponse.JsonRpcResult -> {
                Logger.log("Session update namespaces response received")
                val responseId = wcResponse.response.id
                val namespaces = sessionStorageRepository.getTempNamespaces(responseId)

                sessionStorageRepository.deleteNamespaceAndInsertNewNamespace(session.topic.value, namespaces, responseId,
                    onSuccess = {
                        sessionStorageRepository.markUnAckNamespaceAcknowledged(responseId)
                        scope.launch {
                            _engineEvent.emit(
                                EngineDO.SessionUpdateNamespacesResponse.Result(
                                    session.topic,
                                    session.namespaces.toMapOfEngineNamespacesSession()
                                )
                            )
                        }
                    },
                    onFailure = {
                        scope.launch { _engineEvent.emit(EngineDO.SessionUpdateNamespacesResponse.Error("Unable to update the session")) }
                    })
            }
            is JsonRpcResponse.JsonRpcError -> {
                Logger.error("Peer failed to update session namespaces: ${response.error}")
                scope.launch { _engineEvent.emit(EngineDO.SessionUpdateNamespacesResponse.Error(response.errorMessage)) }
            }
        }
    }

    // listened by DappDelegate
    private fun onSessionRequestResponse(response: WCResponse, params: SessionParamsVO.SessionRequestParams) {
        val result = when (response.response) {
            is JsonRpcResponse.JsonRpcResult -> (response.response as JsonRpcResponse.JsonRpcResult).toEngineDO()
            is JsonRpcResponse.JsonRpcError -> (response.response as JsonRpcResponse.JsonRpcError).toEngineDO()
        }
        val method = params.request.method
        scope.launch { _engineEvent.emit(EngineDO.SessionPayloadResponse(response.topic.value, params.chainId, method, result)) }
    }

    private fun resubscribeToSession() {
        val (listOfExpiredSession, listOfValidSessions) =
            sessionStorageRepository.getListOfSessionVOs().partition { session -> !session.expiry.isSequenceValid() }

        listOfExpiredSession
            .map { session -> session.topic }
            .onEach { sessionTopic ->
                relayer.unsubscribe(sessionTopic)
                crypto.removeKeys(sessionTopic.value)
                sessionStorageRepository.deleteSession(sessionTopic)
            }

        listOfValidSessions
            .onEach { session -> relayer.subscribe(session.topic) }
    }

    private fun setupSequenceExpiration() {
        sessionStorageRepository.onSequenceExpired = { topic ->
            relayer.unsubscribe(topic)
            crypto.removeKeys(topic.value)
        }

        pairingInterface.topicExpiredFlow.onEach { topic ->
            sessionStorageRepository.getSessionByPairingTopic(topic)?.let { sessionTopic ->
                sessionStorageRepository.deleteSession(Topic(sessionTopic))
                relayer.unsubscribe(Topic(sessionTopic))
                crypto.removeKeys(sessionTopic)
            }
        }.launchIn(scope)
    }

    private companion object {
        const val THIRTY_SECONDS_TIMEOUT: Long = 30000L
        const val FIVE_MINUTES_TIMEOUT: Long = 300000L
    }
}