@file:JvmSynthetic

package com.walletconnect.sign.engine.domain

import android.database.sqlite.SQLiteException
import com.walletconnect.android_core.common.*
import com.walletconnect.android_core.common.model.*
import com.walletconnect.android_core.common.model.json_rpc.JsonRpcResponse
import com.walletconnect.android_core.common.model.sync.PendingRequest
import com.walletconnect.android_core.common.model.sync.WCRequest
import com.walletconnect.android_core.common.model.sync.WCResponse
import com.walletconnect.android_core.common.model.type.EngineEvent
import com.walletconnect.android_core.common.model.type.enums.Tags
import com.walletconnect.android_core.common.scope.scope
import com.walletconnect.android_core.crypto.KeyManagementRepository
import com.walletconnect.android_core.utils.*
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.sign.common.exceptions.*
import com.walletconnect.sign.common.exceptions.client.*
import com.walletconnect.sign.common.exceptions.peer.PeerError
import com.walletconnect.sign.common.exceptions.peer.PeerReason
import com.walletconnect.sign.common.model.type.enums.Sequences
import com.walletconnect.sign.common.model.vo.clientsync.common.NamespaceVO
import com.walletconnect.android_core.common.model.RelayProtocolOptions
import com.walletconnect.sign.common.model.vo.clientsync.common.SessionParticipantVO
import com.walletconnect.sign.common.model.vo.clientsync.pairing.PairingRpcVO
import com.walletconnect.sign.common.model.vo.clientsync.pairing.params.PairingParamsVO
import com.walletconnect.sign.common.model.vo.clientsync.session.SessionRpcVO
import com.walletconnect.sign.common.model.vo.clientsync.session.params.SessionParamsVO
import com.walletconnect.sign.common.model.vo.clientsync.session.payload.SessionEventVO
import com.walletconnect.sign.common.model.vo.clientsync.session.payload.SessionRequestVO
import com.walletconnect.sign.common.model.vo.sequence.PairingVO
import com.walletconnect.sign.common.model.vo.sequence.SessionVO
import com.walletconnect.sign.engine.model.EngineDO
import com.walletconnect.sign.engine.model.mapper.*
import com.walletconnect.sign.json_rpc.domain.JsonRpcInteractor
import com.walletconnect.sign.storage.sequence.SequenceStorageRepository
import com.walletconnect.util.bytesToHex
import com.walletconnect.util.generateId
import com.walletconnect.util.randomBytes
import com.walletconnect.utils.Empty
import com.walletconnect.utils.extractTimestamp
import com.walletconnect.utils.isSequenceValid
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import com.walletconnect.android_core.common.WalletConnectException as CoreWalletConnectException

internal class SignEngine(
    private val relayer: JsonRpcInteractor,
    private val crypto: KeyManagementRepository,
    private val sequenceStorageRepository: SequenceStorageRepository,
    private val metaData: EngineDO.AppMetaData,
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
    }

    fun handleInitializationErrors(onError: (CoreWalletConnectException) -> Unit) {
        relayer.initializationErrorsFlow.onEach { walletConnectException -> onError(walletConnectException) }.launchIn(scope)
    }

    internal fun proposeSequence(
        namespaces: Map<String, EngineDO.Namespace.Proposal>,
        relays: List<EngineDO.RelayProtocolOptions>?,
        pairingTopic: String?,
        onProposedSequence: (EngineDO.ProposedSequence) -> Unit,
        onFailure: (Throwable) -> Unit,
    ) {
        fun proposeSession(
            pairingTopic: Topic,
            proposedRelays: List<EngineDO.RelayProtocolOptions>?,
            proposedSequence: EngineDO.ProposedSequence,
        ) {
            Validator.validateProposalNamespace(namespaces.toNamespacesVOProposal()) { error ->
                throw InvalidNamespaceException(error.message)
            }

            val selfPublicKey: PublicKey = crypto.generateKeyPair()
            val sessionProposal = toSessionProposeParams(proposedRelays ?: relays, namespaces, selfPublicKey, metaData)
            val request = PairingRpcVO.SessionPropose(id = generateId(), params = sessionProposal)
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
            if (!sequenceStorageRepository.isPairingValid(Topic(pairingTopic))) {
                throw CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE$pairingTopic")
            }

            val pairing: PairingVO = sequenceStorageRepository.getPairingByTopic(Topic(pairingTopic))
            val relay = EngineDO.RelayProtocolOptions(pairing.relayProtocol, pairing.relayData)

            proposeSession(Topic(pairingTopic), listOf(relay), EngineDO.ProposedSequence.Session)
        } else {
            proposePairing(::proposeSession, onFailure)
        }
    }

    private fun proposePairing(
        proposedSession: (Topic, List<EngineDO.RelayProtocolOptions>?, EngineDO.ProposedSequence) -> Unit,
        onFailure: (Throwable) -> Unit,
    ) {
        val pairingTopic: Topic = generateTopic()
        val symmetricKey: SymmetricKey = crypto.generateSymmetricKey(pairingTopic)
        val relay = RelayProtocolOptions()
        val walletConnectUri = EngineDO.WalletConnectUri(pairingTopic, symmetricKey, relay)
        val pairing = PairingVO.createInactivePairing(pairingTopic, relay, walletConnectUri.toAbsoluteString())

        try {
            sequenceStorageRepository.insertPairing(pairing)
            relayer.subscribe(pairingTopic)

            proposedSession(pairingTopic, null, EngineDO.ProposedSequence.Pairing(walletConnectUri.toAbsoluteString()))
        } catch (e: SQLiteException) {
            crypto.removeKeys(pairingTopic.value)
            relayer.unsubscribe(pairingTopic)
            sequenceStorageRepository.deletePairing(pairingTopic)

            onFailure(e)
        }
    }

    internal fun pair(uri: String) {
        val walletConnectUri: EngineDO.WalletConnectUri = Validator.validateWCUri(uri)
            ?: throw MalformedWalletConnectUri(MALFORMED_PAIRING_URI_MESSAGE)

        if (sequenceStorageRepository.isPairingValid(walletConnectUri.topic)) {
            throw PairWithExistingPairingIsNotAllowed(PAIRING_NOW_ALLOWED_MESSAGE)
        }

        val pairing = PairingVO.createActivePairing(walletConnectUri)
        val symmetricKey = walletConnectUri.symKey
        crypto.setSymmetricKey(walletConnectUri.topic, symmetricKey)

        try {
            relayer.subscribe(pairing.topic)
            sequenceStorageRepository.insertPairing(pairing)
        } catch (e: SQLiteException) {
            crypto.removeKeys(walletConnectUri.topic.value)
            relayer.unsubscribe(pairing.topic)
        }
    }

    internal fun reject(proposerPublicKey: String, reason: String, code: Int, onFailure: (Throwable) -> Unit = {}) {
        val request = sessionProposalRequest[proposerPublicKey]
            ?: throw CannotFindSessionProposalException("$NO_SESSION_PROPOSAL$proposerPublicKey")
        sessionProposalRequest.remove(proposerPublicKey)
        val irnParams = IrnParams(Tags.SESSION_PROPOSE_RESPONSE, Ttl(FIVE_MINUTES_IN_SECONDS))

        relayer.respondWithError(request, PeerError.Error(reason, code), irnParams, onFailure = { error -> onFailure(error) })
    }

    internal fun approve(
        proposerPublicKey: String,
        namespaces: Map<String, EngineDO.Namespace.Session>,
        onFailure: (Throwable) -> Unit = {},
    ) {
        fun sessionSettle(
            requestId: Long,
            proposal: PairingParamsVO.SessionProposeParams,
            sessionTopic: Topic,
        ) {
            val (selfPublicKey, _) = crypto.getKeyAgreement(sessionTopic)
            val selfParticipant = SessionParticipantVO(selfPublicKey.keyAsHex, metaData.toCore())
            val sessionExpiry = ACTIVE_SESSION
            val session = SessionVO.createUnacknowledgedSession(sessionTopic, proposal, selfParticipant, sessionExpiry, namespaces)

            try {
                sequenceStorageRepository.insertSession(session, requestId)
                val params = proposal.toSessionSettleParams(selfParticipant, sessionExpiry, namespaces)
                val sessionSettle = SessionRpcVO.SessionSettle(id = generateId(), params = params)
                val irnParams = IrnParams(Tags.SESSION_SETTLE, Ttl(FIVE_MINUTES_IN_SECONDS))

                relayer.publishJsonRpcRequests(sessionTopic, irnParams, sessionSettle, onFailure = { error -> onFailure(error) })
            } catch (e: SQLiteException) {
                onFailure(e)
            }
        }


        val request = sessionProposalRequest[proposerPublicKey]
            ?: throw CannotFindSessionProposalException("$NO_SESSION_PROPOSAL$proposerPublicKey")
        sessionProposalRequest.remove(proposerPublicKey)
        val proposal = request.params as PairingParamsVO.SessionProposeParams

        Validator.validateSessionNamespace(namespaces.toMapOfNamespacesVOSession(), proposal.namespaces) { error ->
            throw InvalidNamespaceException(error.message)
        }

        val selfPublicKey: PublicKey = crypto.generateKeyPair()
        val sessionTopic = crypto.generateTopicFromKeyAgreement(selfPublicKey, PublicKey(proposerPublicKey))
        relayer.subscribe(sessionTopic)

        val approvalParams = proposal.toSessionApproveParams(selfPublicKey)
        val irnParams = IrnParams(Tags.SESSION_PROPOSE_RESPONSE, Ttl(FIVE_MINUTES_IN_SECONDS))

        relayer.respondWithParams(request, approvalParams, irnParams)

        sessionSettle(request.id, proposal, sessionTopic)
    }

    internal fun sessionUpdate(
        topic: String,
        namespaces: Map<String, EngineDO.Namespace.Session>,
        onFailure: (Throwable) -> Unit,
    ) {
        if (!sequenceStorageRepository.isSessionValid(Topic(topic))) {
            throw CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE$topic")
        }

        val session = sequenceStorageRepository.getSessionByTopic(Topic(topic))

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

        sequenceStorageRepository.insertTempNamespaces(topic, namespaces.toMapOfNamespacesVOSession(), sessionUpdate.id, onSuccess = {
            relayer.publishJsonRpcRequests(Topic(topic), irnParams, sessionUpdate,
                onSuccess = { Logger.log("Update sent successfully") },
                onFailure = { error ->
                    Logger.error("Sending session update error: $error")
                    sequenceStorageRepository.deleteTempNamespacesByTopicAndRequestId(topic, sessionUpdate.id)
                    onFailure(error)
                }
            )
        }, onFailure = {
            onFailure(GenericException("Error updating namespaces"))
        })
    }

    internal fun sessionRequest(request: EngineDO.Request, onFailure: (Throwable) -> Unit) {
        if (!sequenceStorageRepository.isSessionValid(Topic(request.topic))) {
            throw CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE${request.topic}")
        }

        Validator.validateSessionRequest(request) { error ->
            throw InvalidRequestException(error.message)
        }

        val namespaces: Map<String, NamespaceVO.Session> = sequenceStorageRepository.getSessionByTopic(Topic(request.topic)).namespaces
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

    internal fun respondSessionRequest(topic: String, jsonRpcResponse: JsonRpcResponse, onFailure: (Throwable) -> Unit) {
        if (!sequenceStorageRepository.isSessionValid(Topic(topic))) {
            throw CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE$topic")
        }
        val irnParams = IrnParams(Tags.SESSION_REQUEST_RESPONSE, Ttl(FIVE_MINUTES_IN_SECONDS))

        relayer.publishJsonRpcResponse(Topic(topic), jsonRpcResponse, irnParams,
            { Logger.log("Session payload sent successfully") },
            { error ->
                Logger.error("Sending session payload response error: $error")
                onFailure(error)
            })
    }

    internal fun ping(topic: String, onSuccess: (String) -> Unit, onFailure: (Throwable) -> Unit) {
        val (pingPayload, irnParams) = when {
            sequenceStorageRepository.isSessionValid(Topic(topic)) ->
                Pair(
                    SessionRpcVO.SessionPing(id = generateId(), params = SessionParamsVO.PingParams()),
                    IrnParams(Tags.SESSION_PING, Ttl(THIRTY_SECONDS))
                )
            sequenceStorageRepository.isPairingValid(Topic(topic)) ->
                Pair(
                    PairingRpcVO.PairingPing(id = generateId(), params = PairingParamsVO.PingParams()),
                    IrnParams(Tags.PAIRING_PING, Ttl(THIRTY_SECONDS))
                )
            else -> throw CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE$topic")
        }

        relayer.publishJsonRpcRequests(Topic(topic), irnParams, pingPayload,
            onSuccess = {
                Logger.log("Ping sent successfully")
                scope.launch {
                    try {
                        withTimeout(THIRTY_SECONDS_TIMEOUT) {
                            collectResponse(pingPayload.id) { result ->
                                cancel()
                                result.fold(
                                    onSuccess = { onSuccess(topic) },
                                    onFailure = { error -> onFailure(error) })
                            }
                        }
                    } catch (e: TimeoutCancellationException) {
                        onFailure(e)
                    }
                }
            },
            onFailure = { error -> onFailure(error) })
    }

    internal fun emit(topic: String, event: EngineDO.Event, onFailure: (Throwable) -> Unit) {
        if (!sequenceStorageRepository.isSessionValid(Topic(topic))) {
            throw CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE$topic")
        }

        val session = sequenceStorageRepository.getSessionByTopic(Topic(topic))
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
        if (!sequenceStorageRepository.isSessionValid(Topic(topic))) {
            throw CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE$topic")
        }

        val session = sequenceStorageRepository.getSessionByTopic(Topic(topic))
        if (!session.isSelfController) {
            throw UnauthorizedPeerException(UNAUTHORIZED_EXTEND_MESSAGE)
        }
        if (!session.isAcknowledged) {
            throw NotSettledSessionException("$SESSION_IS_NOT_ACKNOWLEDGED_MESSAGE$topic")
        }

        val newExpiration = session.expiry.seconds + WEEK_IN_SECONDS
        sequenceStorageRepository.extendSession(Topic(topic), newExpiration)
        val sessionExtend = SessionRpcVO.SessionExtend(id = generateId(), params = SessionParamsVO.ExtendParams(newExpiration))
        val irnParams = IrnParams(Tags.SESSION_EXTEND, Ttl(DAY_IN_SECONDS))

        relayer.publishJsonRpcRequests(Topic(topic), irnParams, sessionExtend,
            onSuccess = { Logger.error("Session extend sent successfully") },
            onFailure = { error ->
                Logger.error("Sending session extend error: $error")
                onFailure(error)
            })
    }

    internal fun disconnect(topic: String) {
        if (!sequenceStorageRepository.isSessionValid(Topic(topic))) {
            throw CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE$topic")
        }

        val deleteParams = SessionParamsVO.DeleteParams(PeerReason.UserDisconnected.code, PeerReason.UserDisconnected.message)
        val sessionDelete = SessionRpcVO.SessionDelete(id = generateId(), params = deleteParams)
        sequenceStorageRepository.deleteSession(Topic(topic))
        relayer.unsubscribe(Topic(topic))
        val irnParams = IrnParams(Tags.SESSION_DELETE, Ttl(DAY_IN_SECONDS))

        relayer.publishJsonRpcRequests(Topic(topic), irnParams, sessionDelete,
            onSuccess = { Logger.error("Disconnect sent successfully") },
            onFailure = { error -> Logger.error("Sending session disconnect error: $error") })
    }

    internal fun getListOfSettledSessions(): List<EngineDO.Session> {
        return sequenceStorageRepository.getListOfSessionVOs()
            .filter { session -> session.isAcknowledged && session.expiry.isSequenceValid() }
            .map { session -> session.toEngineDOApprovedSessionVO() }
    }

    internal fun getListOfSettledPairings(): List<EngineDO.PairingSettle> {
        return sequenceStorageRepository.getListOfPairingVOs()
            .filter { pairing -> pairing.expiry.isSequenceValid() }
            .map { pairing -> pairing.toEngineDOSettledPairing() }
    }

    internal fun getPendingRequests(topic: Topic): List<PendingRequest> = relayer.getPendingRequests(topic)

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
        scope.launch {
            relayer.clientSyncJsonRpc.collect { request ->
                when (val requestParams = request.params) {
                    is PairingParamsVO.SessionProposeParams -> onSessionPropose(request, requestParams)
                    is PairingParamsVO.DeleteParams -> onPairingDelete(request, requestParams)
                    is SessionParamsVO.SessionSettleParams -> onSessionSettle(request, requestParams)
                    is SessionParamsVO.SessionRequestParams -> onSessionRequest(request, requestParams)
                    is SessionParamsVO.DeleteParams -> onSessionDelete(request, requestParams)
                    is SessionParamsVO.EventParams -> onSessionEvent(request, requestParams)
                    is SessionParamsVO.UpdateNamespacesParams -> onSessionUpdate(request, requestParams)
                    is SessionParamsVO.ExtendParams -> onSessionExtend(request, requestParams)
                    is SessionParamsVO.PingParams, is PairingParamsVO.PingParams -> onPing(request)
                }
            }
        }
    }

    private fun collectInternalErrors() {
        relayer.internalErrors
            .onEach { exception -> _engineEvent.emit(SDKError(exception)) }
            .launchIn(scope)
    }

    private fun onSessionPropose(request: WCRequest, payloadParams: PairingParamsVO.SessionProposeParams) {
        Validator.validateProposalNamespace(payloadParams.namespaces) { error ->
            val irnParams = IrnParams(Tags.SESSION_PROPOSE_RESPONSE, Ttl(FIVE_MINUTES_IN_SECONDS))
            relayer.respondWithError(request, error.toPeerError(), irnParams)
            return
        }

        sessionProposalRequest[payloadParams.proposer.publicKey] = request
        scope.launch { _engineEvent.emit(payloadParams.toEngineDOSessionProposal()) }
    }

    private fun onSessionSettle(request: WCRequest, settleParams: SessionParamsVO.SessionSettleParams) {
        val sessionTopic = request.topic
        val (selfPublicKey, _) = crypto.getKeyAgreement(sessionTopic)
        val peerMetadata = settleParams.controller.metadata
        val proposal = sessionProposalRequest[selfPublicKey.keyAsHex] ?: return
        val irnParams = IrnParams(Tags.SESSION_SETTLE, Ttl(FIVE_MINUTES_IN_SECONDS))

        if (proposal.params !is PairingParamsVO.SessionProposeParams) {
            relayer.respondWithError(request, PeerError.SessionSettlementFailed(NAMESPACE_MISSING_PROPOSAL_MESSAGE), irnParams)
            return
        }

        val proposalNamespaces = (proposal.params as PairingParamsVO.SessionProposeParams).namespaces

        Validator.validateSessionNamespace(settleParams.namespaces, proposalNamespaces) { error ->
            relayer.respondWithError(request, error.toPeerError(), irnParams)
            return
        }

        val tempProposalRequest = sessionProposalRequest.getValue(selfPublicKey.keyAsHex)

        try {
            val session =
                SessionVO.createAcknowledgedSession(sessionTopic, settleParams, selfPublicKey, metaData.toCore(), proposalNamespaces)

            sequenceStorageRepository.upsertPairingPeerMetadata(proposal.topic, peerMetadata)
            sessionProposalRequest.remove(selfPublicKey.keyAsHex)
            sequenceStorageRepository.insertSession(session, request.id)
            val irnParams = IrnParams(Tags.SESSION_SETTLE, Ttl(FIVE_MINUTES_IN_SECONDS))

            relayer.respondWithSuccess(request, irnParams)
            scope.launch { _engineEvent.emit(session.toSessionApproved()) }
        } catch (e: SQLiteException) {
            sessionProposalRequest[selfPublicKey.keyAsHex] = tempProposalRequest
            sequenceStorageRepository.deleteSession(sessionTopic)
            relayer.respondWithError(request, PeerError.SessionSettlementFailed(e.message ?: String.Empty), irnParams)
            return
        }
    }

    private fun onPairingDelete(request: WCRequest, params: PairingParamsVO.DeleteParams) {
        if (!sequenceStorageRepository.isPairingValid(request.topic)) {
            val irnParams = IrnParams(Tags.PAIRING_DELETE_RESPONSE, Ttl(DAY_IN_SECONDS))
            relayer.respondWithError(request, PeerError.NoMatchingTopic(Sequences.PAIRING.name, request.topic.value), irnParams)
            return
        }

        crypto.removeKeys(request.topic.value)
        relayer.unsubscribe(request.topic)
        sequenceStorageRepository.deletePairing(request.topic)

        scope.launch { _engineEvent.emit(EngineDO.DeletedPairing(request.topic.value, params.message)) }
    }

    private fun onSessionDelete(request: WCRequest, params: SessionParamsVO.DeleteParams) {
        if (!sequenceStorageRepository.isSessionValid(request.topic)) {
            val irnParams = IrnParams(Tags.SESSION_DELETE_RESPONSE, Ttl(DAY_IN_SECONDS))
            relayer.respondWithError(request, PeerError.NoMatchingTopic(Sequences.SESSION.name, request.topic.value), irnParams)
            return
        }

        crypto.removeKeys(request.topic.value)
        sequenceStorageRepository.deleteSession(request.topic)
        relayer.unsubscribe(request.topic)

        scope.launch { _engineEvent.emit(params.toEngineDoDeleteSession(request.topic)) }
    }

    private fun onSessionRequest(request: WCRequest, params: SessionParamsVO.SessionRequestParams) {
        val irnParams = IrnParams(Tags.SESSION_REQUEST_RESPONSE, Ttl(FIVE_MINUTES_IN_SECONDS))
        Validator.validateSessionRequest(params.toEngineDORequest(request.topic)) { error ->
            relayer.respondWithError(request, error.toPeerError(), irnParams)
            return
        }

        if (!sequenceStorageRepository.isSessionValid(request.topic)) {
            relayer.respondWithError(request, PeerError.NoMatchingTopic(Sequences.SESSION.name, request.topic.value), irnParams)
            return
        }

        val (sessionNamespaces: Map<String, NamespaceVO.Session>, sessionPeerMetaData: com.walletconnect.android_core.common.model.MetaData?) =
            with(sequenceStorageRepository.getSessionByTopic(request.topic)) { namespaces to peerMetaData }

        val method = params.request.method
        Validator.validateChainIdWithMethodAuthorisation(params.chainId, method, sessionNamespaces) { error ->
            relayer.respondWithError(request, error.toPeerError(), irnParams)
            return
        }

        scope.launch { _engineEvent.emit(params.toEngineDOSessionRequest(request, sessionPeerMetaData)) }
    }

    private fun onSessionEvent(request: WCRequest, params: SessionParamsVO.EventParams) {
        val irnParams = IrnParams(Tags.SESSION_EVENT_RESPONSE, Ttl(FIVE_MINUTES_IN_SECONDS))
        Validator.validateEvent(params.toEngineDOEvent()) { error ->
            relayer.respondWithError(request, error.toPeerError(), irnParams)
            return
        }

        if (!sequenceStorageRepository.isSessionValid(request.topic)) {
            relayer.respondWithError(request, PeerError.NoMatchingTopic(Sequences.SESSION.name, request.topic.value), irnParams)
            return
        }

        val session = sequenceStorageRepository.getSessionByTopic(request.topic)
        if (!session.isPeerController) {
            relayer.respondWithError(request, PeerError.UnauthorizedEventEmit(Sequences.SESSION.name), irnParams)
            return
        }
        if (!session.isAcknowledged) {
            relayer.respondWithError(request, PeerError.NoMatchingTopic(Sequences.SESSION.name, request.topic.value), irnParams)
            return
        }

        val event = params.event
        Validator.validateChainIdWithEventAuthorisation(params.chainId, event.name, session.namespaces) { error ->
            relayer.respondWithError(request, error.toPeerError(), irnParams)
            return
        }

        relayer.respondWithSuccess(request, irnParams)
        scope.launch { _engineEvent.emit(params.toEngineDOSessionEvent(request.topic)) }
    }

    private fun onSessionUpdate(request: WCRequest, params: SessionParamsVO.UpdateNamespacesParams) {
        val irnParams = IrnParams(Tags.SESSION_UPDATE_RESPONSE, Ttl(DAY_IN_SECONDS))
        if (!sequenceStorageRepository.isSessionValid(request.topic)) {
            relayer.respondWithError(request, PeerError.NoMatchingTopic(Sequences.SESSION.name, request.topic.value), irnParams)
            return
        }

        val session: SessionVO = sequenceStorageRepository.getSessionByTopic(request.topic)
        if (!session.isPeerController) {
            relayer.respondWithError(request, PeerError.UnauthorizedUpdateRequest(Sequences.SESSION.name), irnParams)
            return
        }

        Validator.validateSessionNamespace(params.namespaces, session.proposalNamespaces) { error ->
            relayer.respondWithError(request, PeerError.InvalidUpdateRequest(error.message), irnParams)
            return
        }

        if (!sequenceStorageRepository.isUpdatedNamespaceValid(session.topic.value, request.id.extractTimestamp())) {
            relayer.respondWithError(request, PeerError.InvalidUpdateRequest("Update Namespace Request ID too old"), irnParams)
            return
        }

        sequenceStorageRepository.deleteNamespaceAndInsertNewNamespace(session.topic.value, params.namespaces, request.id, onSuccess = {
            relayer.respondWithSuccess(request, irnParams)

            scope.launch {
                _engineEvent.emit(EngineDO.SessionUpdateNamespaces(request.topic, params.namespaces.toMapOfEngineNamespacesSession()))
            }
        }, onFailure = {
            relayer.respondWithError(
                request,
                PeerError.InvalidUpdateRequest("Updating Namespace Failed. Review Namespace structure"),
                irnParams
            )
        })
    }

    private fun onSessionExtend(request: WCRequest, requestParams: SessionParamsVO.ExtendParams) {
        val irnParams = IrnParams(Tags.SESSION_EXTEND_RESPONSE, Ttl(DAY_IN_SECONDS))
        if (!sequenceStorageRepository.isSessionValid(request.topic)) {
            relayer.respondWithError(request, PeerError.NoMatchingTopic(Sequences.SESSION.name, request.topic.value), irnParams)
            return
        }

        val session = sequenceStorageRepository.getSessionByTopic(request.topic)
        if (!session.isPeerController) {
            relayer.respondWithError(request, PeerError.UnauthorizedExtendRequest(Sequences.SESSION.name), irnParams)
            return
        }

        val newExpiry = requestParams.expiry
        Validator.validateSessionExtend(newExpiry, session.expiry.seconds) { error ->
            relayer.respondWithError(request, error.toPeerError(), irnParams)
            return
        }

        sequenceStorageRepository.extendSession(request.topic, newExpiry)
        relayer.respondWithSuccess(request, irnParams)
        scope.launch { _engineEvent.emit(session.toEngineDOSessionExtend(Expiry(newExpiry))) }
    }

    private fun onPing(request: WCRequest) {
        val irnParams = IrnParams(Tags.SESSION_PING_RESPONSE, Ttl(THIRTY_SECONDS))
        relayer.respondWithSuccess(request, irnParams)
    }

    private fun collectJsonRpcResponses() {
        scope.launch {
            relayer.peerResponse.collect { response ->
                when (val params = response.params) {
                    is PairingParamsVO.SessionProposeParams -> onSessionProposalResponse(response, params)
                    is SessionParamsVO.SessionSettleParams -> onSessionSettleResponse(response)
                    is SessionParamsVO.UpdateNamespacesParams -> onSessionUpdateResponse(response)
                    is SessionParamsVO.SessionRequestParams -> onSessionRequestResponse(response, params)
                }
            }
        }
    }

    private fun onSessionProposalResponse(wcResponse: WCResponse, params: PairingParamsVO.SessionProposeParams) {
        val pairingTopic = wcResponse.topic
        if (!sequenceStorageRepository.isPairingValid(pairingTopic)) return
        val pairing = sequenceStorageRepository.getPairingByTopic(pairingTopic)
        if (!pairing.isActive) {
            sequenceStorageRepository.activatePairing(pairingTopic, ACTIVE_PAIRING)
        }

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
                if (!pairing.isActive) sequenceStorageRepository.deletePairing(pairingTopic)
                Logger.log("Session proposal reject received: ${response.error}")
                scope.launch { _engineEvent.emit(EngineDO.SessionRejected(pairingTopic.value, response.errorMessage)) }
            }
        }
    }

    private fun onSessionSettleResponse(wcResponse: WCResponse) {
        val sessionTopic = wcResponse.topic
        if (!sequenceStorageRepository.isSessionValid(sessionTopic)) return
        val session = sequenceStorageRepository.getSessionByTopic(sessionTopic)

        when (wcResponse.response) {
            is JsonRpcResponse.JsonRpcResult -> {
                Logger.log("Session settle success received")
                sequenceStorageRepository.acknowledgeSession(sessionTopic)
                scope.launch { _engineEvent.emit(EngineDO.SettledSessionResponse.Result(session.toEngineDOApprovedSessionVO())) }
            }
            is JsonRpcResponse.JsonRpcError -> {
                Logger.error("Peer failed to settle session: ${(wcResponse.response as JsonRpcResponse.JsonRpcError).errorMessage}")
                relayer.unsubscribe(sessionTopic)
                sequenceStorageRepository.deleteSession(sessionTopic)
                crypto.removeKeys(sessionTopic.value)
            }
        }
    }

    private fun onSessionUpdateResponse(wcResponse: WCResponse) {
        val sessionTopic = wcResponse.topic
        if (!sequenceStorageRepository.isSessionValid(sessionTopic)) return
        val session = sequenceStorageRepository.getSessionByTopic(sessionTopic)
        if (!sequenceStorageRepository.isUpdatedNamespaceResponseValid(session.topic.value, wcResponse.response.id.extractTimestamp())) {
            return
        }

        when (val response = wcResponse.response) {
            is JsonRpcResponse.JsonRpcResult -> {
                Logger.log("Session update namespaces response received")
                val responseId = wcResponse.response.id
                val sessionTopic = session.topic.value
                val namespaces = sequenceStorageRepository.getTempNamespaces(responseId)

                sequenceStorageRepository.deleteNamespaceAndInsertNewNamespace(sessionTopic, namespaces, responseId,
                    onSuccess = {
                        sequenceStorageRepository.markUnAckNamespaceAcknowledged(responseId)
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

    private fun onSessionRequestResponse(response: WCResponse, params: SessionParamsVO.SessionRequestParams) {
        val result = when (response.response) {
            is JsonRpcResponse.JsonRpcResult -> (response.response as JsonRpcResponse.JsonRpcResult).toEngineJsonRpcResult()
            is JsonRpcResponse.JsonRpcError -> (response.response as JsonRpcResponse.JsonRpcError).toEngineJsonRpcError()
        }
        val method = params.request.method
        scope.launch { _engineEvent.emit(EngineDO.SessionPayloadResponse(response.topic.value, params.chainId, method, result)) }
    }

    private fun resubscribeToSequences() {
        relayer.isConnectionAvailable
            .onEach { isAvailable -> _engineEvent.emit(ConnectionState(isAvailable)) }
            .filter { isAvailable: Boolean -> isAvailable }
            .onEach {
                coroutineScope {
                    launch(Dispatchers.IO) { resubscribeToPairings() }
                    launch(Dispatchers.IO) { resubscribeToSession() }
                }
            }
            .launchIn(scope)
    }

    private fun resubscribeToPairings() {
        val (listOfExpiredPairing, listOfValidPairing) =
            sequenceStorageRepository.getListOfPairingVOs().partition { pairing -> !pairing.expiry.isSequenceValid() }

        listOfExpiredPairing
            .map { pairing -> pairing.topic }
            .onEach { pairingTopic ->
                relayer.unsubscribe(pairingTopic)
                crypto.removeKeys(pairingTopic.value)
                sequenceStorageRepository.deletePairing(pairingTopic)
            }

        listOfValidPairing
            .map { pairing -> pairing.topic }
            .onEach { pairingTopic -> relayer.subscribe(pairingTopic) }
    }

    private fun resubscribeToSession() {
        val (listOfExpiredSession, listOfValidSessions) =
            sequenceStorageRepository.getListOfSessionVOs().partition { session -> !session.expiry.isSequenceValid() }

        listOfExpiredSession
            .map { session -> session.topic }
            .onEach { sessionTopic ->
                relayer.unsubscribe(sessionTopic)
                crypto.removeKeys(sessionTopic.value)
                sequenceStorageRepository.deleteSession(sessionTopic)
            }

        listOfValidSessions
            .onEach { session -> relayer.subscribe(session.topic) }
    }

    private fun setupSequenceExpiration() {
        sequenceStorageRepository.onSequenceExpired = { topic ->
            relayer.unsubscribe(topic)
            crypto.removeKeys(topic.value)
        }
    }

    private fun generateTopic(): Topic = Topic(randomBytes(32).bytesToHex())

    private companion object {
        const val THIRTY_SECONDS_TIMEOUT: Long = 30000L
        const val FIVE_MINUTES_TIMEOUT: Long = 300000L
    }
}