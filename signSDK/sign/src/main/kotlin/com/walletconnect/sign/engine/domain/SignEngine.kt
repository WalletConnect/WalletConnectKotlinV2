@file:JvmSynthetic

package com.walletconnect.sign.engine.domain

import android.database.sqlite.SQLiteException
import com.walletconnect.sign.core.exceptions.*
import com.walletconnect.sign.core.exceptions.client.WalletConnectException
import com.walletconnect.sign.core.exceptions.peer.PeerError
import com.walletconnect.sign.core.exceptions.peer.PeerReason
import com.walletconnect.sign.core.model.type.EngineEvent
import com.walletconnect.sign.core.model.type.enums.Sequences
import com.walletconnect.sign.core.model.vo.ExpiryVO
import com.walletconnect.sign.core.model.vo.PublicKey
import com.walletconnect.sign.core.model.vo.SymmetricKey
import com.walletconnect.sign.core.model.vo.TopicVO
import com.walletconnect.sign.core.model.vo.clientsync.common.MetaDataVO
import com.walletconnect.sign.core.model.vo.clientsync.common.NamespaceVO
import com.walletconnect.sign.core.model.vo.clientsync.common.RelayProtocolOptionsVO
import com.walletconnect.sign.core.model.vo.clientsync.common.SessionParticipantVO
import com.walletconnect.sign.core.model.vo.clientsync.pairing.PairingSettlementVO
import com.walletconnect.sign.core.model.vo.clientsync.pairing.params.PairingParamsVO
import com.walletconnect.sign.core.model.vo.clientsync.session.SessionSettlementVO
import com.walletconnect.sign.core.model.vo.clientsync.session.params.SessionParamsVO
import com.walletconnect.sign.core.model.vo.clientsync.session.payload.SessionEventVO
import com.walletconnect.sign.core.model.vo.clientsync.session.payload.SessionRequestVO
import com.walletconnect.sign.core.model.vo.jsonRpc.JsonRpcResponseVO
import com.walletconnect.sign.core.model.vo.sequence.PairingVO
import com.walletconnect.sign.core.model.vo.sequence.SessionVO
import com.walletconnect.sign.core.model.vo.sync.PendingRequestVO
import com.walletconnect.sign.core.model.vo.sync.WCRequestVO
import com.walletconnect.sign.core.model.vo.sync.WCResponseVO
import com.walletconnect.sign.core.scope.scope
import com.walletconnect.sign.crypto.KeyManagementRepository
import com.walletconnect.sign.engine.model.EngineDO
import com.walletconnect.sign.engine.model.mapper.*
import com.walletconnect.sign.relay.domain.RelayerInteractor
import com.walletconnect.sign.storage.sequence.SequenceStorageRepository
import com.walletconnect.sign.util.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

internal class SignEngine(
    private val relayer: RelayerInteractor,
    private val crypto: KeyManagementRepository,
    private val sequenceStorageRepository: SequenceStorageRepository,
    private val metaData: EngineDO.AppMetaData,
) {
    private val _engineEvent: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val engineEvent: SharedFlow<EngineEvent> = _engineEvent.asSharedFlow()
    private val sessionProposalRequest: MutableMap<String, WCRequestVO> = mutableMapOf()

    init {
        resubscribeToSequences()
        setupSequenceExpiration()
        collectJsonRpcRequests()
        collectJsonRpcResponses()
        collectInternalErrors()
    }

    fun handleInitializationErrors(onError: (WalletConnectException) -> Unit) {
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
            pairingTopic: TopicVO,
            proposedRelays: List<EngineDO.RelayProtocolOptions>?,
            proposedSequence: EngineDO.ProposedSequence,
        ) {
            Validator.validateProposalNamespace(namespaces.toNamespacesVOProposal()) { error ->
                throw WalletConnectException.InvalidNamespaceException(error.message)
            }

            val selfPublicKey: PublicKey = crypto.generateKeyPair()
            val sessionProposal = toSessionProposeParams(proposedRelays ?: relays, namespaces, selfPublicKey, metaData)
            val request = PairingSettlementVO.SessionPropose(id = generateId(), params = sessionProposal)
            sessionProposalRequest[selfPublicKey.keyAsHex] = WCRequestVO(pairingTopic, request.id, request.method, sessionProposal)

            relayer.publishJsonRpcRequests(pairingTopic, request,
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
            if (!sequenceStorageRepository.isPairingValid(TopicVO(pairingTopic))) {
                throw WalletConnectException.CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE$pairingTopic")
            }

            val pairing: PairingVO = sequenceStorageRepository.getPairingByTopic(TopicVO(pairingTopic))
            val relay = EngineDO.RelayProtocolOptions(pairing.relayProtocol, pairing.relayData)

            proposeSession(TopicVO(pairingTopic), listOf(relay), EngineDO.ProposedSequence.Session)
        } else {
            proposePairing(::proposeSession, onFailure)
        }
    }

    private fun proposePairing(
        proposedSession: (TopicVO, List<EngineDO.RelayProtocolOptions>?, EngineDO.ProposedSequence) -> Unit,
        onFailure: (Throwable) -> Unit,
    ) {
        val pairingTopic: TopicVO = generateTopic()
        val symmetricKey: SymmetricKey = crypto.generateSymmetricKey(pairingTopic)
        val relay = RelayProtocolOptionsVO()
        val walletConnectUri = EngineDO.WalletConnectUri(pairingTopic, symmetricKey, relay)
        val pairing = PairingVO.createInactivePairing(pairingTopic, relay, walletConnectUri.toAbsoluteString())

        try {
            sequenceStorageRepository.insertPairing(pairing)
            relayer.subscribe(pairingTopic)

            proposedSession(pairingTopic, null, EngineDO.ProposedSequence.Pairing(walletConnectUri.toAbsoluteString()))
        } catch (e: SQLiteException) {
            crypto.removeKeys(pairingTopic.value)
            relayer.unsubscribe(pairingTopic)

            onFailure(e)
        }
    }

    internal fun pair(uri: String) {
        val walletConnectUri: EngineDO.WalletConnectUri = Validator.validateWCUri(uri)
            ?: throw WalletConnectException.MalformedWalletConnectUri(MALFORMED_PAIRING_URI_MESSAGE)

        if (sequenceStorageRepository.isPairingValid(walletConnectUri.topic)) {
            throw WalletConnectException.PairWithExistingPairingIsNotAllowed(PAIRING_NOW_ALLOWED_MESSAGE)
        }

        val pairing = PairingVO.createActivePairing(walletConnectUri)
        val symmetricKey = walletConnectUri.symKey
        crypto.setSymmetricKey(walletConnectUri.topic, symmetricKey)

        try {
            sequenceStorageRepository.insertPairing(pairing)
            relayer.subscribe(pairing.topic)
        } catch (e: SQLiteException) {
            crypto.removeKeys(walletConnectUri.topic.value)
            relayer.unsubscribe(pairing.topic)
        }
    }

    internal fun reject(proposerPublicKey: String, reason: String, code: Int, onFailure: (Throwable) -> Unit = {}) {
        val request = sessionProposalRequest[proposerPublicKey]
            ?: throw WalletConnectException.CannotFindSessionProposalException("$NO_SESSION_PROPOSAL$proposerPublicKey")
        sessionProposalRequest.remove(proposerPublicKey)
        relayer.respondWithError(request, PeerError.Error(reason, code), onFailure = { error -> onFailure(error) })
    }

    internal fun approve(
        proposerPublicKey: String,
        namespaces: Map<String, EngineDO.Namespace.Session>,
        onFailure: (Throwable) -> Unit = {},
    ) {
        fun sessionSettle(
            requestId: Long,
            proposal: PairingParamsVO.SessionProposeParams,
            sessionTopic: TopicVO,
        ) {
            val (selfPublicKey, _) = crypto.getKeyAgreement(sessionTopic)
            val selfParticipant = SessionParticipantVO(selfPublicKey.keyAsHex, metaData.toMetaDataVO())
            val sessionExpiry = Expiration.activeSession
            val session = SessionVO.createUnacknowledgedSession(sessionTopic, proposal, selfParticipant, sessionExpiry, namespaces)

            try {
                sequenceStorageRepository.insertSession(session, requestId)
                val params = proposal.toSessionSettleParams(selfParticipant, sessionExpiry, namespaces)
                val sessionSettle = SessionSettlementVO.SessionSettle(id = generateId(), params = params)

                relayer.publishJsonRpcRequests(sessionTopic, sessionSettle, onFailure = { error -> onFailure(error) })
            } catch (e: SQLiteException) {
                onFailure(e)
            }
        }


        val request = sessionProposalRequest[proposerPublicKey]
            ?: throw WalletConnectException.CannotFindSessionProposalException("$NO_SESSION_PROPOSAL$proposerPublicKey")
        sessionProposalRequest.remove(proposerPublicKey)
        val proposal = request.params as PairingParamsVO.SessionProposeParams

        Validator.validateSessionNamespace(namespaces.toMapOfNamespacesVOSession(), proposal.namespaces) { error ->
            throw WalletConnectException.InvalidNamespaceException(error.message)
        }

        val selfPublicKey: PublicKey = crypto.generateKeyPair()
        val sessionTopic = crypto.generateTopicFromKeyAgreement(selfPublicKey, PublicKey(proposerPublicKey))
        relayer.subscribe(sessionTopic)

        val approvalParams = proposal.toSessionApproveParams(selfPublicKey)
        relayer.respondWithParams(request, approvalParams)

        sessionSettle(request.id, proposal, sessionTopic)
    }

    internal fun updateSession(
        topic: String,
        namespaces: Map<String, EngineDO.Namespace.Session>,
        onFailure: (Throwable) -> Unit,
    ) {
        if (!sequenceStorageRepository.isSessionValid(TopicVO(topic))) {
            throw WalletConnectException.CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE$topic")
        }

        val session = sequenceStorageRepository.getSessionByTopic(TopicVO(topic))

        if (!session.isSelfController) {
            throw WalletConnectException.UnauthorizedPeerException(UNAUTHORIZED_UPDATE_MESSAGE)
        }

        if (!session.isAcknowledged) {
            throw WalletConnectException.NotSettledSessionException("$SESSION_IS_NOT_ACKNOWLEDGED_MESSAGE$topic")
        }

        Validator.validateSessionNamespace(namespaces.toMapOfNamespacesVOSession(), session.proposalNamespaces) { error ->
            throw WalletConnectException.InvalidNamespaceException(error.message)
        }

        val params = SessionParamsVO.UpdateNamespacesParams(namespaces.toMapOfNamespacesVOSession())
        val sessionUpdate = SessionSettlementVO.SessionUpdate(id = generateId(), params = params)

        sequenceStorageRepository.insertUnAckNamespaces(topic, namespaces.toMapOfNamespacesVOSession(), sessionUpdate.id, onSuccess = {
            relayer.publishJsonRpcRequests(TopicVO(topic), sessionUpdate,
                onSuccess = { Logger.log("Update sent successfully") },
                onFailure = { error ->
                    Logger.error("Sending session update error: $error")
                    onFailure(error)
                }
            )
        }, onFailure = {
            onFailure(WalletConnectException.GenericException("Error updating namespaces"))
        })
    }

    internal fun sessionRequest(request: EngineDO.Request, onFailure: (Throwable) -> Unit) {
        if (!sequenceStorageRepository.isSessionValid(TopicVO(request.topic))) {
            throw WalletConnectException.CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE${request.topic}")
        }

        Validator.validateSessionRequest(request) { error ->
            throw WalletConnectException.InvalidRequestException(error.message)
        }

        val namespaces: Map<String, NamespaceVO.Session> = sequenceStorageRepository.getSessionByTopic(TopicVO(request.topic)).namespaces
        Validator.validateChainIdWithMethodAuthorisation(request.chainId, request.method, namespaces) { error ->
            throw WalletConnectException.UnauthorizedMethodException(error.message)
        }

        val params = SessionParamsVO.SessionRequestParams(SessionRequestVO(request.method, request.params), request.chainId)
        val sessionPayload = SessionSettlementVO.SessionRequest(id = generateId(), params = params)

        relayer.publishJsonRpcRequests(
            topic = TopicVO(request.topic),
            payload = sessionPayload,
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

    internal fun respondSessionRequest(topic: String, jsonRpcResponse: JsonRpcResponseVO, onFailure: (Throwable) -> Unit) {
        if (!sequenceStorageRepository.isSessionValid(TopicVO(topic))) {
            throw WalletConnectException.CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE$topic")
        }

        relayer.publishJsonRpcResponse(TopicVO(topic), jsonRpcResponse,
            { Logger.log("Session payload sent successfully") },
            { error ->
                Logger.error("Sending session payload response error: $error")
                onFailure(error)
            })
    }

    internal fun ping(topic: String, onSuccess: (String) -> Unit, onFailure: (Throwable) -> Unit) {
        val pingParams = when {
            sequenceStorageRepository.isSessionValid(TopicVO(topic)) ->
                SessionSettlementVO.SessionPing(id = generateId(), params = SessionParamsVO.PingParams())
            sequenceStorageRepository.isPairingValid(TopicVO(topic)) ->
                PairingSettlementVO.PairingPing(id = generateId(), params = PairingParamsVO.PingParams())
            else -> throw WalletConnectException.CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE$topic")
        }

        relayer.publishJsonRpcRequests(TopicVO(topic), pingParams,
            onSuccess = {
                Logger.log("Ping sent successfully")
                scope.launch {
                    try {
                        withTimeout(THIRTY_SECONDS_TIMEOUT) {
                            collectResponse(pingParams.id) { result ->
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
        if (!sequenceStorageRepository.isSessionValid(TopicVO(topic))) {
            throw WalletConnectException.CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE$topic")
        }

        val session = sequenceStorageRepository.getSessionByTopic(TopicVO(topic))
        if (!session.isSelfController) {
            throw WalletConnectException.UnauthorizedPeerException(UNAUTHORIZED_EMIT_MESSAGE)
        }

        Validator.validateEvent(event) { error ->
            throw WalletConnectException.InvalidEventException(error.message)
        }

        val namespaces = session.namespaces
        Validator.validateChainIdWithEventAuthorisation(event.chainId, event.name, namespaces) { error ->
            throw WalletConnectException.UnauthorizedEventException(error.message)
        }

        val eventParams = SessionParamsVO.EventParams(SessionEventVO(event.name, event.data), event.chainId)
        val sessionEvent = SessionSettlementVO.SessionEvent(id = generateId(), params = eventParams)

        relayer.publishJsonRpcRequests(TopicVO(topic), sessionEvent,
            onSuccess = { Logger.log("Event sent successfully") },
            onFailure = { error ->
                Logger.error("Sending event error: $error")
                onFailure(error)
            }
        )
    }

    internal fun extend(topic: String, onFailure: (Throwable) -> Unit) {
        if (!sequenceStorageRepository.isSessionValid(TopicVO(topic))) {
            throw WalletConnectException.CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE$topic")
        }

        val session = sequenceStorageRepository.getSessionByTopic(TopicVO(topic))
        if (!session.isSelfController) {
            throw WalletConnectException.UnauthorizedPeerException(UNAUTHORIZED_EXTEND_MESSAGE)
        }
        if (!session.isAcknowledged) {
            throw WalletConnectException.NotSettledSessionException("$SESSION_IS_NOT_ACKNOWLEDGED_MESSAGE$topic")
        }

        val newExpiration = session.expiry.seconds + Time.weekInSeconds
        sequenceStorageRepository.extendSession(TopicVO(topic), newExpiration)
        val sessionExtend = SessionSettlementVO.SessionExtend(id = generateId(), params = SessionParamsVO.ExtendParams(newExpiration))
        relayer.publishJsonRpcRequests(TopicVO(topic), sessionExtend,
            onSuccess = { Logger.error("Session extend sent successfully") },
            onFailure = { error ->
                Logger.error("Sending session extend error: $error")
                onFailure(error)
            })
    }

    internal fun disconnect(topic: String) {
        if (!sequenceStorageRepository.isSessionValid(TopicVO(topic))) {
            throw WalletConnectException.CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE$topic")
        }

        val deleteParams = SessionParamsVO.DeleteParams(PeerReason.UserDisconnected.code, PeerReason.UserDisconnected.message)
        val sessionDelete = SessionSettlementVO.SessionDelete(id = generateId(), params = deleteParams)
        sequenceStorageRepository.deleteSession(TopicVO(topic))

        relayer.unsubscribe(TopicVO(topic))

        relayer.publishJsonRpcRequests(TopicVO(topic), sessionDelete,
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

    internal fun getPendingRequests(topic: TopicVO): List<PendingRequestVO> = relayer.getPendingRequests(topic)

    private suspend fun collectResponse(id: Long, onResponse: (Result<JsonRpcResponseVO.JsonRpcResult>) -> Unit = {}) {
        relayer.peerResponse
            .filter { response -> response.response.id == id }
            .collect { response ->
                when (val result = response.response) {
                    is JsonRpcResponseVO.JsonRpcResult -> onResponse(Result.success(result))
                    is JsonRpcResponseVO.JsonRpcError -> onResponse(Result.failure(Throwable(result.errorMessage)))
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
        relayer.internalErrors.onEach { exception ->
            _engineEvent.emit(EngineDO.InternalError(exception))
        }.launchIn(scope)
    }

    private fun onSessionPropose(request: WCRequestVO, payloadParams: PairingParamsVO.SessionProposeParams) {
        Validator.validateProposalNamespace(payloadParams.namespaces) { error ->
            relayer.respondWithError(request, error.toPeerError())
            return
        }

        sessionProposalRequest[payloadParams.proposer.publicKey] = request
        scope.launch { _engineEvent.emit(payloadParams.toEngineDOSessionProposal()) }
    }

    private fun onSessionSettle(request: WCRequestVO, settleParams: SessionParamsVO.SessionSettleParams) {
        val sessionTopic = request.topic
        val (selfPublicKey, _) = crypto.getKeyAgreement(sessionTopic)
        val peerMetadata = settleParams.controller.metadata
        val proposal = sessionProposalRequest[selfPublicKey.keyAsHex] ?: return

        if (proposal.params !is PairingParamsVO.SessionProposeParams) {
            relayer.respondWithError(request, PeerError.SessionSettlementFailed(NAMESPACE_MISSING_PROPOSAL_MESSAGE))
            return
        }

        val proposalNamespaces = proposal.params.namespaces

        Validator.validateSessionNamespace(settleParams.namespaces, proposalNamespaces) { error ->
            relayer.respondWithError(request, error.toPeerError())
            return
        }

        val tempProposalRequest = sessionProposalRequest.getValue(selfPublicKey.keyAsHex)

        try {
            val session =
                SessionVO.createAcknowledgedSession(sessionTopic, settleParams, selfPublicKey, metaData.toMetaDataVO(), proposalNamespaces)

            sequenceStorageRepository.upsertPairingPeerMetadata(proposal.topic, peerMetadata)
            sessionProposalRequest.remove(selfPublicKey.keyAsHex)
            sequenceStorageRepository.insertSession(session, request.id)
            relayer.respondWithSuccess(request)

            scope.launch { _engineEvent.emit(session.toSessionApproved()) }
        } catch (e: SQLiteException) {
            sessionProposalRequest[selfPublicKey.keyAsHex] = tempProposalRequest
            sequenceStorageRepository.deleteSession(sessionTopic)
            relayer.respondWithError(request, PeerError.SessionSettlementFailed(e.message ?: String.Empty))
            return
        }
    }

    private fun onPairingDelete(request: WCRequestVO, params: PairingParamsVO.DeleteParams) {
        if (!sequenceStorageRepository.isPairingValid(request.topic)) {
            relayer.respondWithError(request, PeerError.NoMatchingTopic(Sequences.PAIRING.name, request.topic.value))
            return
        }

        crypto.removeKeys(request.topic.value)
        relayer.unsubscribe(request.topic)
        sequenceStorageRepository.deletePairing(request.topic)

        scope.launch { _engineEvent.emit(EngineDO.DeletedPairing(request.topic.value, params.message)) }
    }

    private fun onSessionDelete(request: WCRequestVO, params: SessionParamsVO.DeleteParams) {
        if (!sequenceStorageRepository.isSessionValid(request.topic)) {
            relayer.respondWithError(request, PeerError.NoMatchingTopic(Sequences.SESSION.name, request.topic.value))
            return
        }

        crypto.removeKeys(request.topic.value)
        sequenceStorageRepository.deleteSession(request.topic)
        relayer.unsubscribe(request.topic)

        scope.launch { _engineEvent.emit(params.toEngineDoDeleteSession(request.topic)) }
    }

    private fun onSessionRequest(request: WCRequestVO, params: SessionParamsVO.SessionRequestParams) {
        Validator.validateSessionRequest(params.toEngineDORequest(request.topic)) { error ->
            relayer.respondWithError(request, error.toPeerError())
            return
        }

        if (!sequenceStorageRepository.isSessionValid(request.topic)) {
            relayer.respondWithError(request, PeerError.NoMatchingTopic(Sequences.SESSION.name, request.topic.value))
            return
        }

        val (sessionNamespaces: Map<String, NamespaceVO.Session>, sessionPeerMetaData: MetaDataVO?) =
            with(sequenceStorageRepository.getSessionByTopic(request.topic)) { namespaces to peerMetaData }

        val method = params.request.method
        Validator.validateChainIdWithMethodAuthorisation(params.chainId, method, sessionNamespaces) { error ->
            relayer.respondWithError(request, error.toPeerError())
            return
        }

        scope.launch { _engineEvent.emit(params.toEngineDOSessionRequest(request, sessionPeerMetaData)) }
    }

    private fun onSessionEvent(request: WCRequestVO, params: SessionParamsVO.EventParams) {
        Validator.validateEvent(params.toEngineDOEvent()) { error ->
            relayer.respondWithError(request, error.toPeerError())
            return
        }

        if (!sequenceStorageRepository.isSessionValid(request.topic)) {
            relayer.respondWithError(request, PeerError.NoMatchingTopic(Sequences.SESSION.name, request.topic.value))
            return
        }

        val session = sequenceStorageRepository.getSessionByTopic(request.topic)
        if (!session.isPeerController) {
            relayer.respondWithError(request, PeerError.UnauthorizedEventEmit(Sequences.SESSION.name))
            return
        }
        if (!session.isAcknowledged) {
            relayer.respondWithError(request, PeerError.NoMatchingTopic(Sequences.SESSION.name, request.topic.value))
            return
        }

        val event = params.event
        Validator.validateChainIdWithEventAuthorisation(params.chainId, event.name, session.namespaces) { error ->
            relayer.respondWithError(request, error.toPeerError())
            return
        }

        relayer.respondWithSuccess(request)
        scope.launch { _engineEvent.emit(params.toEngineDOSessionEvent(request.topic)) }
    }

    private fun onSessionUpdate(request: WCRequestVO, params: SessionParamsVO.UpdateNamespacesParams) {
        if (!sequenceStorageRepository.isSessionValid(request.topic)) {
            relayer.respondWithError(request, PeerError.NoMatchingTopic(Sequences.SESSION.name, request.topic.value))
            return
        }

        val session: SessionVO = sequenceStorageRepository.getSessionByTopic(request.topic)
        if (!session.isPeerController) {
            relayer.respondWithError(request, PeerError.UnauthorizedUpdateRequest(Sequences.SESSION.name))
            return
        }

        Validator.validateSessionNamespace(params.namespaces, session.proposalNamespaces) { error ->
            relayer.respondWithError(request, PeerError.InvalidUpdateRequest(error.message))
            return
        }

        if (!sequenceStorageRepository.isUpdatedNamespaceValid(session.topic.value, request.id.extractTimestamp())) {
            relayer.respondWithError(request, PeerError.InvalidUpdateRequest("Update Namespace Request ID too old"))
            return
        }

        sequenceStorageRepository.deleteNamespaceAndInsertNewNamespace(session.topic.value, params.namespaces, request.id, onSuccess = {
            relayer.respondWithSuccess(request)

            scope.launch {
                _engineEvent.emit(EngineDO.SessionUpdateNamespaces(request.topic, params.namespaces.toMapOfEngineNamespacesSession()))
            }
        }, onFailure = {
            relayer.respondWithError(request, PeerError.InvalidUpdateRequest("Updating Namespace Failed. Review Namespace structure"))
        })
    }

    private fun onSessionExtend(request: WCRequestVO, requestParams: SessionParamsVO.ExtendParams) {
        if (!sequenceStorageRepository.isSessionValid(request.topic)) {
            relayer.respondWithError(request, PeerError.NoMatchingTopic(Sequences.SESSION.name, request.topic.value))
            return
        }

        val session = sequenceStorageRepository.getSessionByTopic(request.topic)
        if (!session.isPeerController) {
            relayer.respondWithError(request, PeerError.UnauthorizedExtendRequest(Sequences.SESSION.name))
            return
        }

        val newExpiry = requestParams.expiry
        Validator.validateSessionExtend(newExpiry, session.expiry.seconds) { error ->
            relayer.respondWithError(request, error.toPeerError())
            return
        }

        sequenceStorageRepository.extendSession(request.topic, newExpiry)
        relayer.respondWithSuccess(request)
        scope.launch { _engineEvent.emit(session.toEngineDOSessionExtend(ExpiryVO(newExpiry))) }
    }

    private fun onPing(request: WCRequestVO) {
        relayer.respondWithSuccess(request)
    }

    private fun collectJsonRpcResponses() {
        scope.launch {
            relayer.peerResponse.collect { response ->
                when (val params = response.params) {
                    is PairingParamsVO.SessionProposeParams -> onSessionProposalResponse(response, params)
                    is SessionParamsVO.SessionSettleParams -> onSessionSettleResponse(response)
                    is SessionParamsVO.UpdateNamespacesParams -> onSessionUpdateNamespacesResponse(response)
                    is SessionParamsVO.SessionRequestParams -> onSessionRequestResponse(response, params)
                }
            }
        }
    }

    private fun onSessionProposalResponse(wcResponse: WCResponseVO, params: PairingParamsVO.SessionProposeParams) {
        val pairingTopic = wcResponse.topic
        if (!sequenceStorageRepository.isPairingValid(pairingTopic)) return
        val pairing = sequenceStorageRepository.getPairingByTopic(pairingTopic)
        if (!pairing.isActive) {
            sequenceStorageRepository.activatePairing(pairingTopic, Expiration.activePairing)
        }

        when (val response = wcResponse.response) {
            is JsonRpcResponseVO.JsonRpcResult -> {
                Logger.log("Session proposal approve received")
                val selfPublicKey = PublicKey(params.proposer.publicKey)
                val approveParams = response.result as SessionParamsVO.ApprovalParams
                val responderPublicKey = PublicKey(approveParams.responderPublicKey)
                val sessionTopic = crypto.generateTopicFromKeyAgreement(selfPublicKey, responderPublicKey)
                relayer.subscribe(sessionTopic)
            }
            is JsonRpcResponseVO.JsonRpcError -> {
                if (!pairing.isActive) sequenceStorageRepository.deletePairing(pairingTopic)
                Logger.log("Session proposal reject received: ${response.error}")
                scope.launch { _engineEvent.emit(EngineDO.SessionRejected(pairingTopic.value, response.errorMessage)) }
            }
        }
    }

    private fun onSessionSettleResponse(wcResponse: WCResponseVO) {
        val sessionTopic = wcResponse.topic
        if (!sequenceStorageRepository.isSessionValid(sessionTopic)) return
        val session = sequenceStorageRepository.getSessionByTopic(sessionTopic)

        when (wcResponse.response) {
            is JsonRpcResponseVO.JsonRpcResult -> {
                Logger.log("Session settle success received")
                sequenceStorageRepository.acknowledgeSession(sessionTopic)
                scope.launch { _engineEvent.emit(EngineDO.SettledSessionResponse.Result(session.toEngineDOApprovedSessionVO())) }
            }
            is JsonRpcResponseVO.JsonRpcError -> {
                Logger.error("Peer failed to settle session: ${wcResponse.response.errorMessage}")
                relayer.unsubscribe(sessionTopic)
                sequenceStorageRepository.deleteSession(sessionTopic)
                crypto.removeKeys(sessionTopic.value)
            }
        }
    }

    private fun onSessionUpdateNamespacesResponse(wcResponse: WCResponseVO) {
        val sessionTopic = wcResponse.topic
        if (!sequenceStorageRepository.isSessionValid(sessionTopic)) return
        val session = sequenceStorageRepository.getSessionByTopic(sessionTopic)
        if (!sequenceStorageRepository.isUpdatedNamespaceResponseValid(session.topic.value, wcResponse.response.id.extractTimestamp())) {
            return
        }

        when (val response = wcResponse.response) {
            is JsonRpcResponseVO.JsonRpcResult -> {
                Logger.log("Session update namespaces response received")

                val updatedNamespace = sequenceStorageRepository.getUnAckNamespaces(session.topic.value, wcResponse.response.id)
                sequenceStorageRepository.deleteNamespaceAndInsertNewNamespace(session.topic.value,
                    updatedNamespace,
                    wcResponse.response.id,
                    onSuccess = {

                        sequenceStorageRepository.markUnAckNamespaceAcknowledged(session.topic.value, wcResponse.response.id)

                        scope.launch {
                            _engineEvent.emit(
                                EngineDO.SessionUpdateNamespacesResponse.Result(session.topic,
                                    session.namespaces.toMapOfEngineNamespacesSession())
                            )
                        }
                    },
                    onFailure = {
                        scope.launch { _engineEvent.emit(EngineDO.SessionUpdateNamespacesResponse.Error("Unable to update the session")) }
                    })
            }
            is JsonRpcResponseVO.JsonRpcError -> {
                Logger.error("Peer failed to update session namespaces: ${response.error}")
                scope.launch { _engineEvent.emit(EngineDO.SessionUpdateNamespacesResponse.Error(response.errorMessage)) }
            }
        }
    }

    private fun onSessionRequestResponse(response: WCResponseVO, params: SessionParamsVO.SessionRequestParams) {
        val result = when (response.response) {
            is JsonRpcResponseVO.JsonRpcResult -> response.response.toEngineJsonRpcResult()
            is JsonRpcResponseVO.JsonRpcError -> response.response.toEngineJsonRpcError()
        }
        val method = params.request.method
        scope.launch { _engineEvent.emit(EngineDO.SessionPayloadResponse(response.topic.value, params.chainId, method, result)) }
    }

    private fun resubscribeToSequences() {
        relayer.isConnectionAvailable
            .onEach { isAvailable ->
                _engineEvent.emit(EngineDO.ConnectionState(isAvailable))
            }
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

    private fun generateTopic(): TopicVO = TopicVO(randomBytes(32).bytesToHex())

    private companion object {
        const val THIRTY_SECONDS_TIMEOUT: Long = 30000L
        const val FIVE_MINUTES_TIMEOUT: Long = 300000L
    }
}