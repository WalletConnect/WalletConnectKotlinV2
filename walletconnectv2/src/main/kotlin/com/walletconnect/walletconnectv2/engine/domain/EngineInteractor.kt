package com.walletconnect.walletconnectv2.engine.domain

import com.walletconnect.walletconnectv2.core.exceptions.client.*
import com.walletconnect.walletconnectv2.core.exceptions.peer.PeerError
import com.walletconnect.walletconnectv2.core.model.type.SequenceLifecycle
import com.walletconnect.walletconnectv2.core.model.type.enums.Sequences
import com.walletconnect.walletconnectv2.core.model.vo.ExpiryVO
import com.walletconnect.walletconnectv2.core.model.vo.PublicKey
import com.walletconnect.walletconnectv2.core.model.vo.SecretKey
import com.walletconnect.walletconnectv2.core.model.vo.TopicVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.common.RelayProtocolOptionsVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.common.SessionParticipantVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.pairing.PairingSettlementVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.pairing.params.PairingParamsVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.SessionSettlementVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.params.SessionParamsVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.payload.SessionEventVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.payload.SessionRequestVO
import com.walletconnect.walletconnectv2.core.model.vo.jsonRpc.JsonRpcResponseVO
import com.walletconnect.walletconnectv2.core.model.vo.sequence.PairingVO
import com.walletconnect.walletconnectv2.core.model.vo.sequence.SessionVO
import com.walletconnect.walletconnectv2.core.model.vo.sync.PendingRequestVO
import com.walletconnect.walletconnectv2.core.model.vo.sync.WCRequestVO
import com.walletconnect.walletconnectv2.core.model.vo.sync.WCResponseVO
import com.walletconnect.walletconnectv2.core.scope.scope
import com.walletconnect.walletconnectv2.crypto.CryptoRepository
import com.walletconnect.walletconnectv2.engine.model.EngineDO
import com.walletconnect.walletconnectv2.engine.model.mapper.*
import com.walletconnect.walletconnectv2.relay.domain.WalletConnectRelayer
import com.walletconnect.walletconnectv2.storage.sequence.SequenceStorageRepository
import com.walletconnect.walletconnectv2.util.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

internal class EngineInteractor(
    private val relayer: WalletConnectRelayer,
    private val crypto: CryptoRepository,
    private val sequenceStorageRepository: SequenceStorageRepository,
    private val metaData: EngineDO.AppMetaData,
) {
    private val _sequenceEvent: MutableSharedFlow<SequenceLifecycle> = MutableSharedFlow()
    val sequenceEvent: SharedFlow<SequenceLifecycle> = _sequenceEvent
    private val sessionProposalRequest: MutableMap<String, WCRequestVO> = mutableMapOf()

    init {
        resubscribeToSettledSequences()
        setupSequenceExpiration()
        collectJsonRpcRequests()
        collectJsonRpcResponses()
    }

    fun handleInitializationErrors(onError: (WalletConnectException) -> Unit) {
        relayer.initializationErrorsFlow.onEach { walletConnectException -> onError(walletConnectException) }.launchIn(scope)
    }

    internal fun proposeSequence(
        namespaces: List<EngineDO.Namespace>,
        relays: List<EngineDO.RelayProtocolOptions>?,
        pairingTopic: String?,
        onProposedSequence: (EngineDO.ProposedSequence) -> Unit,
        onFailure: (Throwable) -> Unit,
    ) {
        Validator.validateCAIP2(namespaces) { errorMessage ->
            throw WalletConnectException.InvalidSessionChainIdsException(errorMessage)
        }

        if (pairingTopic != null) {
            if (!sequenceStorageRepository.isPairingValid(TopicVO(pairingTopic))) {
                throw WalletConnectException.CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE$pairingTopic")
            }
            val pairing: PairingVO = sequenceStorageRepository.getPairingByTopic(TopicVO(pairingTopic))
            val relay = EngineDO.RelayProtocolOptions(pairing.relayProtocol, pairing.relayData)

            proposeSession(namespaces, TopicVO(pairingTopic), listOf(relay),
                onSuccess = { onProposedSequence(EngineDO.ProposedSequence.Session) },
                onFailure = { error -> onFailure(error) })

        } else {
            proposePairing(namespaces, relays,
                onSessionProposeSuccess = { pairing -> onProposedSequence(pairing) },
                onFailure = { error -> onFailure(error) })
        }
    }

    private fun proposePairing(
        namespaces: List<EngineDO.Namespace>,
        relays: List<EngineDO.RelayProtocolOptions>?,
        onSessionProposeSuccess: (EngineDO.ProposedSequence.Pairing) -> Unit,
        onFailure: (Throwable) -> Unit,
    ) {
        val pairingTopic: TopicVO = generateTopic()
        val symmetricKey: SecretKey = crypto.generateSymmetricKey(pairingTopic)
        val relay = RelayProtocolOptionsVO()
        val walletConnectUri = EngineDO.WalletConnectUri(pairingTopic, symmetricKey, relay)
        val pairing = PairingVO.createPairing(pairingTopic, relay, walletConnectUri.toAbsoluteString())
        sequenceStorageRepository.insertPairing(pairing)
        relayer.subscribe(pairingTopic)

        proposeSession(namespaces, pairingTopic, relays,
            onSuccess = { onSessionProposeSuccess(EngineDO.ProposedSequence.Pairing(walletConnectUri.toAbsoluteString())) },
            onFailure = { error -> onFailure(error) })
    }

    private fun proposeSession(
        namespaces: List<EngineDO.Namespace>,
        pairingTopic: TopicVO,
        relays: List<EngineDO.RelayProtocolOptions>?,
        onFailure: (Throwable) -> Unit = {},
        onSuccess: () -> Unit = {},
    ) {
        val selfPublicKey: PublicKey = crypto.generateKeyPair()
        val sessionProposal = toSessionProposeParams(relays, namespaces, selfPublicKey, metaData)
        val request = PairingSettlementVO.SessionPropose(id = generateId(), params = sessionProposal)
        sessionProposalRequest[selfPublicKey.keyAsHex] = WCRequestVO(pairingTopic, request.id, request.method, sessionProposal)

        relayer.publishJsonRpcRequests(pairingTopic, request,
            onSuccess = {
                Logger.error("Session proposal sent successfully")
                onSuccess()
            },
            onFailure = { error ->
                Logger.error("Failed to send a session proposal: $error")
                onFailure(error)
            })
    }

    internal fun pair(uri: String) {
        val walletConnectUri: EngineDO.WalletConnectUri = Validator.validateWCUri(uri)
            ?: throw WalletConnectException.MalformedWalletConnectUri(MALFORMED_PAIRING_URI_MESSAGE)

        if (sequenceStorageRepository.isPairingValid(walletConnectUri.topic)) {
            throw WalletConnectException.PairWithExistingPairingIsNotAllowed(PAIRING_NOW_ALLOWED_MESSAGE)
        }

        val pairing = PairingVO.createFromUri(walletConnectUri)
        val symmetricKey = walletConnectUri.symKey
        crypto.setSymmetricKey(walletConnectUri.topic, symmetricKey)
        sequenceStorageRepository.insertPairing(pairing)
        relayer.subscribe(pairing.topic)
    }

    internal fun reject(proposerPublicKey: String, reason: String, code: Int, onFailure: (Throwable) -> Unit = {}) {
        val request = sessionProposalRequest[proposerPublicKey]
            ?: throw WalletConnectException.CannotFindSessionProposalException("$NO_SESSION_PROPOSAL$proposerPublicKey")
        sessionProposalRequest.remove(proposerPublicKey)
        relayer.respondWithError(request, PeerError.Error(reason, code), onFailure = { error -> onFailure(error) })
    }

    internal fun approve(
        proposerPublicKey: String,
        accounts: List<String>,
        namespaces: List<EngineDO.Namespace>,
        onFailure: (Throwable) -> Unit,
    ) {
        val request = sessionProposalRequest[proposerPublicKey]
            ?: throw WalletConnectException.CannotFindSessionProposalException("$NO_SESSION_PROPOSAL$proposerPublicKey")
        sessionProposalRequest.remove(proposerPublicKey)
        val proposal = request.params as PairingParamsVO.SessionProposeParams

        Validator.validateCAIP10(accounts) { errorMessage ->
            throw WalletConnectException.InvalidAccountsException(errorMessage)
        }

        val selfPublicKey: PublicKey = crypto.generateKeyPair()
        val (_, sessionTopic) = crypto.generateTopicAndSharedKey(selfPublicKey, PublicKey(proposerPublicKey))
        relayer.subscribe(sessionTopic)

        val approvalParams = proposal.toSessionApproveParams(selfPublicKey)
        relayer.respondWithParams(request, approvalParams)

        sessionSettle(proposal, accounts, namespaces, sessionTopic) { error -> onFailure(error) }
    }

    internal fun updateSessionAccounts(topic: String, newAccounts: List<String>, onFailure: (Throwable) -> Unit) {
        if (!sequenceStorageRepository.isSessionValid(TopicVO(topic))) {
            throw WalletConnectException.CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE$topic")
        }

        Validator.validateCAIP10(newAccounts) { errorMessage ->
            throw WalletConnectException.InvalidAccountsException(errorMessage)
        }

        val session = sequenceStorageRepository.getSessionByTopic(TopicVO(topic))
        if (!session.isSelfController) {
            throw WalletConnectException.UnauthorizedPeerException(UNAUTHORIZED_UPDATE_MESSAGE)
        }
        if (!session.isAcknowledged) {
            throw WalletConnectException.NotSettledSessionException("$SESSION_IS_NOT_ACKNOWLEDGED_MESSAGE$topic")
        }

        val params = SessionParamsVO.UpdateAccountsParams(newAccounts)
        val sessionUpdateAccounts = SessionSettlementVO.SessionUpdateAccounts(id = generateId(), params = params)
        sequenceStorageRepository.updateSessionWithAccounts(TopicVO(topic), newAccounts)

        relayer.publishJsonRpcRequests(TopicVO(topic), sessionUpdateAccounts,
            onSuccess = { Logger.log("Update accounts sent successfully") },
            onFailure = { error ->
                Logger.error("Sending session update error: $error")
                onFailure(error)
            }
        )
    }

    internal fun updateSessionMethods(topic: String, methods: List<String>, onFailure: (Throwable) -> Unit) {
        if (!sequenceStorageRepository.isSessionValid(TopicVO(topic))) {
            throw WalletConnectException.CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE$topic")
        }

        Validator.validateMethods(methods) { errorMessage ->
            throw WalletConnectException.InvalidSessionMethodsException(errorMessage)
        }

        val session = sequenceStorageRepository.getSessionByTopic(TopicVO(topic))
        if (!session.isSelfController) {
            throw WalletConnectException.UnauthorizedPeerException(UNAUTHORIZED_UPDATE_MESSAGE)
        }
        if (!session.isAcknowledged) {
            throw WalletConnectException.NotSettledSessionException("$SESSION_IS_NOT_ACKNOWLEDGED_MESSAGE$topic")
        }

        val params = SessionParamsVO.UpdateMethodsParams(methods)
        val sessionUpdateMethods = SessionSettlementVO.SessionUpdateMethods(id = generateId(), params = params)
        sequenceStorageRepository.updateSessionWithMethods(TopicVO(topic), methods)

        relayer.publishJsonRpcRequests(TopicVO(topic), sessionUpdateMethods,
            onSuccess = { Logger.log("Update methods sent successfully") },
            onFailure = { error ->
                Logger.error("Sending session update error: $error")
                onFailure(error)
            }
        )
    }

    internal fun updateSessionEvents(topic: String, events: List<String>, onFailure: (Throwable) -> Unit) {
        if (!sequenceStorageRepository.isSessionValid(TopicVO(topic))) {
            throw WalletConnectException.CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE$topic")
        }

        Validator.validateEvents(events) { errorMessage ->
            throw WalletConnectException.InvalidSessionEventsException(errorMessage)
        }

        val session = sequenceStorageRepository.getSessionByTopic(TopicVO(topic))
        if (!session.isSelfController) {
            throw WalletConnectException.UnauthorizedPeerException(UNAUTHORIZED_UPDATE_MESSAGE)
        }
        if (!session.isAcknowledged) {
            throw WalletConnectException.NotSettledSessionException("$SESSION_IS_NOT_ACKNOWLEDGED_MESSAGE$topic")
        }

        val params = SessionParamsVO.UpdateEventsParams(events)
        val sessionUpdateEvents = SessionSettlementVO.SessionUpdateEvents(id = generateId(), params = params)
        sequenceStorageRepository.updateSessionWithEvents(TopicVO(topic), events)

        relayer.publishJsonRpcRequests(TopicVO(topic), sessionUpdateEvents,
            onSuccess = { Logger.log("Update events sent successfully") },
            onFailure = { error ->
                Logger.error("Sending session update error: $error")
                onFailure(error)
            }
        )
    }

    internal fun sessionRequest(request: EngineDO.Request, onFailure: (Throwable) -> Unit) {
        if (!sequenceStorageRepository.isSessionValid(TopicVO(request.topic))) {
            throw WalletConnectException.CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE${request.topic}")
        }

        val chains: List<String> = sequenceStorageRepository.getSessionByTopic(TopicVO(request.topic)).chains
        Validator.validateChainIdAuthorization(request.chainId, chains) { errorMessage ->
            throw WalletConnectException.UnauthorizedChainIdException(errorMessage)
        }

        val params =
            SessionParamsVO.SessionRequestParams(request = SessionRequestVO(request.method, request.params), chainId = request.chainId)
        val sessionPayload = SessionSettlementVO.SessionRequest(id = generateId(), params = params)

        relayer.publishJsonRpcRequests(
            TopicVO(request.topic), sessionPayload,
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

        Validator.validateEvent(event) { errorMessage ->
            throw WalletConnectException.InvalidEventException(errorMessage)
        }

        val session = sequenceStorageRepository.getSessionByTopic(TopicVO(topic))
        Validator.validateEventAuthorization(session, event.name) { errorMessage ->
            throw WalletConnectException.UnauthorizedEventException(errorMessage)
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

    fun updateSessionExpiry(topic: String, newExpiration: Long, onFailure: (Throwable) -> Unit) {
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

        Validator.validateSessionExtend(newExpiration, session.expiry.seconds) { errorMessage ->
            throw WalletConnectException.InvalidExtendException(errorMessage)
        }

        sequenceStorageRepository.updateSessionExpiry(TopicVO(topic), newExpiration)
        val sessionExtend =
            SessionSettlementVO.SessionUpdateExpiry(id = generateId(), params = SessionParamsVO.UpdateExpiryParams(expiry = newExpiration))
        relayer.publishJsonRpcRequests(TopicVO(topic), sessionExtend,
            onSuccess = { Logger.error("Session extend sent successfully") },
            onFailure = { error ->
                Logger.error("Sending session extend error: $error")
                onFailure(error)
            })
    }

    internal fun disconnect(topic: String, reason: String, code: Int) {
        if (!sequenceStorageRepository.isSessionValid(TopicVO(topic))) {
            throw WalletConnectException.CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE$topic")
        }

        val deleteParams = SessionParamsVO.DeleteParams(message = reason, code = code)
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

    private fun sessionSettle(
        proposal: PairingParamsVO.SessionProposeParams,
        accounts: List<String>,
        namespaces: List<EngineDO.Namespace>,
        sessionTopic: TopicVO,
        onFailure: (Throwable) -> Unit,
    ) {
        val (selfPublicKey, _) = crypto.getKeyAgreement(sessionTopic)
        val selfParticipant = SessionParticipantVO(selfPublicKey.keyAsHex, metaData.toMetaDataVO())
        val sessionExpiry = Expiration.activeSession
        val session =
            SessionVO.createUnacknowledgedSession(sessionTopic, proposal, selfParticipant, sessionExpiry, accounts, namespaces)
        sequenceStorageRepository.insertSession(session)
        val params = proposal.toSessionSettleParams(selfParticipant, sessionExpiry, accounts, namespaces)
        val sessionSettle = SessionSettlementVO.SessionSettle(id = generateId(), params = params)

        relayer.publishJsonRpcRequests(sessionTopic, sessionSettle, onFailure = { error -> onFailure(error) })
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
                    is SessionParamsVO.UpdateAccountsParams -> onSessionUpdateAccounts(request, requestParams)
                    is SessionParamsVO.UpdateMethodsParams -> onSessionUpdateMethods(request, requestParams)
                    is SessionParamsVO.UpdateEventsParams -> onSessionUpdateEvents(request, requestParams)
                    is SessionParamsVO.UpdateExpiryParams -> onSessionUpdateExpiry(request, requestParams)
                    is SessionParamsVO.PingParams, is PairingParamsVO.PingParams -> onPing(request)
                }
            }
        }
    }

    private fun onSessionPropose(request: WCRequestVO, payloadParams: PairingParamsVO.SessionProposeParams) {
        sessionProposalRequest[payloadParams.proposer.publicKey] = request
        scope.launch { _sequenceEvent.emit(payloadParams.toEngineDOSessionProposal()) }
    }

    private fun onSessionSettle(request: WCRequestVO, settleParams: SessionParamsVO.SessionSettleParams) {
        val sessionTopic = request.topic
        val (selfPublicKey, _) = crypto.getKeyAgreement(sessionTopic)
        val peerMetadata = settleParams.controller.metadata
        val proposal = sessionProposalRequest[selfPublicKey.keyAsHex] ?: return
        sequenceStorageRepository.updatePairingPeerMetadata(proposal.topic, peerMetadata)
        sessionProposalRequest.remove(selfPublicKey.keyAsHex)

        val session = SessionVO.createAcknowledgedSession(sessionTopic, settleParams, selfPublicKey, metaData.toMetaDataVO())
        sequenceStorageRepository.insertSession(session)
        relayer.respondWithSuccess(request)

        scope.launch { _sequenceEvent.emit(session.toSessionApproved()) }
    }

    private fun onPairingDelete(request: WCRequestVO, params: PairingParamsVO.DeleteParams) {
        if (!sequenceStorageRepository.isPairingValid(request.topic)) {
            relayer.respondWithError(request, PeerError.NoMatchingTopic(Sequences.PAIRING.name, request.topic.value))
            return
        }

        crypto.removeKeys(request.topic.value)
        relayer.unsubscribe(request.topic)
        sequenceStorageRepository.deletePairing(request.topic)
        scope.launch { _sequenceEvent.emit(EngineDO.DeletedPairing(request.topic.value, params.message)) }
    }

    private fun onSessionDelete(request: WCRequestVO, params: SessionParamsVO.DeleteParams) {
        if (!sequenceStorageRepository.isSessionValid(request.topic)) {
            relayer.respondWithError(request, PeerError.NoMatchingTopic(Sequences.SESSION.name, request.topic.value))
            return
        }

        crypto.removeKeys(request.topic.value)
        sequenceStorageRepository.deleteSession(request.topic)
        relayer.unsubscribe(request.topic)
        scope.launch { _sequenceEvent.emit(params.toEngineDoDeleteSession(request.topic)) }
    }

    private fun onSessionRequest(request: WCRequestVO, params: SessionParamsVO.SessionRequestParams) {
        if (!sequenceStorageRepository.isSessionValid(request.topic)) {
            relayer.respondWithError(request, PeerError.NoMatchingTopic(Sequences.SESSION.name, request.topic.value))
            return
        }

        val session = sequenceStorageRepository.getSessionByTopic(request.topic)
        if (params.chainId != null && !session.chains.contains(params.chainId)) {
            relayer.respondWithError(request, PeerError.UnauthorizedTargetChainId(params.chainId))
            return
        }

        val method = params.request.method
        if (!session.methods.contains(method)) {
            relayer.respondWithError(request, PeerError.UnauthorizedJsonRpcMethod(method))
            return
        }
        scope.launch { _sequenceEvent.emit(params.toEngineDOSessionRequest(request, session.peerMetaData)) }
    }

    private fun onSessionEvent(request: WCRequestVO, params: SessionParamsVO.EventParams) {
        if (!sequenceStorageRepository.isSessionValid(request.topic)) {
            relayer.respondWithError(request, PeerError.NoMatchingTopic(Sequences.SESSION.name, request.topic.value))
            return
        }

        val session = sequenceStorageRepository.getSessionByTopic(request.topic)
        if (!session.isAcknowledged) {
            relayer.respondWithError(request, PeerError.NoMatchingTopic(Sequences.SESSION.name, request.topic.value))
            return
        }

        val event = params.event
        Validator.validateChainIdAuthorization(params.chainId, session.chains) {
            relayer.respondWithError(request, PeerError.UnauthorizedEventRequest(event.name))
            return@validateChainIdAuthorization
        }

        Validator.validateEventAuthorization(session, event.name) {
            relayer.respondWithError(request, PeerError.UnauthorizedEventRequest(event.name))
            return@validateEventAuthorization
        }

        relayer.respondWithSuccess(request)
        scope.launch { _sequenceEvent.emit(params.toEngineDOSessionEvent(request.topic)) }
    }

    private fun onSessionUpdateAccounts(request: WCRequestVO, params: SessionParamsVO.UpdateAccountsParams) {
        if (!sequenceStorageRepository.isSessionValid(request.topic)) {
            relayer.respondWithError(request, PeerError.NoMatchingTopic(Sequences.SESSION.name, request.topic.value))
            return
        }

        val session: SessionVO = sequenceStorageRepository.getSessionByTopic(request.topic)
        if (!session.isPeerController) {
            relayer.respondWithError(request, PeerError.UnauthorizedUpdateAccountsRequest(Sequences.SESSION.name))
            return
        }

        Validator.validateCAIP10(params.accounts) {
            relayer.respondWithError(request, PeerError.InvalidUpdateAccountsRequest(Sequences.SESSION.name))
            return@validateCAIP10
        }

        sequenceStorageRepository.updateSessionWithAccounts(session.topic, params.accounts)
        relayer.respondWithSuccess(request)

        scope.launch { _sequenceEvent.emit(EngineDO.SessionUpdateAccounts(request.topic, params.accounts)) }
    }

    private fun onSessionUpdateMethods(request: WCRequestVO, params: SessionParamsVO.UpdateMethodsParams) {
        if (!sequenceStorageRepository.isSessionValid(request.topic)) {
            relayer.respondWithError(request, PeerError.NoMatchingTopic(Sequences.SESSION.name, request.topic.value))
            return
        }

        val session: SessionVO = sequenceStorageRepository.getSessionByTopic(request.topic)
        if (!session.isPeerController) {
            relayer.respondWithError(request, PeerError.UnauthorizedUpdateMethodsRequest(Sequences.SESSION.name))
            return
        }

        Validator.validateMethods(params.methods) {
            relayer.respondWithError(request, PeerError.InvalidUpdateMethodsRequest(Sequences.SESSION.name))
            return@validateMethods
        }

        sequenceStorageRepository.updateSessionWithMethods(session.topic, params.methods)
        relayer.respondWithSuccess(request)

        scope.launch { _sequenceEvent.emit(EngineDO.SessionUpdateMethods(request.topic, params.methods)) }
    }

    private fun onSessionUpdateEvents(request: WCRequestVO, params: SessionParamsVO.UpdateEventsParams) {
        if (!sequenceStorageRepository.isSessionValid(request.topic)) {
            relayer.respondWithError(request, PeerError.NoMatchingTopic(Sequences.SESSION.name, request.topic.value))
            return
        }

        val session: SessionVO = sequenceStorageRepository.getSessionByTopic(request.topic)
        if (!session.isPeerController) {
            relayer.respondWithError(request, PeerError.UnauthorizedUpdateEventsRequest(Sequences.SESSION.name))
            return
        }

        Validator.validateEvents(params.events) {
            relayer.respondWithError(request, PeerError.InvalidUpdateEventsRequest(Sequences.SESSION.name))
            return@validateEvents
        }

        sequenceStorageRepository.updateSessionWithEvents(session.topic, params.events)
        relayer.respondWithSuccess(request)

        scope.launch { _sequenceEvent.emit(EngineDO.SessionUpdateEvents(request.topic, params.events)) }
    }

    private fun onSessionUpdateExpiry(request: WCRequestVO, requestParams: SessionParamsVO.UpdateExpiryParams) {
        if (!sequenceStorageRepository.isSessionValid(request.topic)) {
            relayer.respondWithError(request, PeerError.NoMatchingTopic(Sequences.SESSION.name, request.topic.value))
            return
        }

        val session = sequenceStorageRepository.getSessionByTopic(request.topic)
        if (!session.isSelfController) {
            relayer.respondWithError(request, PeerError.UnauthorizedUpdateExpiryRequest(Sequences.SESSION.name))
            return
        }

        val newExpiry = requestParams.expiry
        Validator.validateSessionExtend(newExpiry, session.expiry.seconds) {
            relayer.respondWithError(request, PeerError.InvalidUpdateExpiryRequest(Sequences.SESSION.name))
            return@validateSessionExtend
        }

        sequenceStorageRepository.updateSessionExpiry(request.topic, newExpiry)
        relayer.respondWithSuccess(request)
        scope.launch { _sequenceEvent.emit(session.toEngineDOSessionUpdateExpiry(ExpiryVO(newExpiry))) }
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
                    is SessionParamsVO.UpdateAccountsParams -> onSessionUpdateAccountsResponse(response)
                    is SessionParamsVO.UpdateMethodsParams -> onSessionUpdateMethodsResponse(response)
                    is SessionParamsVO.UpdateEventsParams -> onSessionUpdateEventsResponse(response)
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
                val (_, sessionTopic) = crypto.generateTopicAndSharedKey(selfPublicKey, responderPublicKey)
                relayer.subscribe(sessionTopic)
            }
            is JsonRpcResponseVO.JsonRpcError -> {
                if (!pairing.isActive) sequenceStorageRepository.deletePairing(pairingTopic)
                Logger.log("Session proposal reject received: ${response.error}")
                scope.launch { _sequenceEvent.emit(EngineDO.SessionRejected(pairingTopic.value, response.errorMessage)) }
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
                scope.launch { _sequenceEvent.emit(EngineDO.SettledSessionResponse.Result(session.toEngineDOApprovedSessionVO(sessionTopic))) }
            }
            is JsonRpcResponseVO.JsonRpcError -> {
                Logger.error("Peer failed to settle session: ${wcResponse.response.errorMessage}")
                relayer.unsubscribe(sessionTopic)
                sequenceStorageRepository.deleteSession(sessionTopic)
                crypto.removeKeys(sessionTopic.value)
            }
        }
    }

    private fun onSessionUpdateAccountsResponse(wcResponse: WCResponseVO) {
        val sessionTopic = wcResponse.topic
        if (!sequenceStorageRepository.isSessionValid(sessionTopic)) return
        val session = sequenceStorageRepository.getSessionByTopic(sessionTopic)

        when (val response = wcResponse.response) {
            is JsonRpcResponseVO.JsonRpcResult -> {
                Logger.log("Session update accounts response received")
                scope.launch { _sequenceEvent.emit(EngineDO.SessionUpdateAccountsResponse.Result(session.topic, session.accounts)) }
            }
            is JsonRpcResponseVO.JsonRpcError -> {
                Logger.error("Peer failed to update session accounts: ${response.error}")
                scope.launch { _sequenceEvent.emit(EngineDO.SessionUpdateAccountsResponse.Error(response.errorMessage)) }
            }
        }
    }

    private fun onSessionUpdateMethodsResponse(wcResponse: WCResponseVO) {
        val sessionTopic = wcResponse.topic
        if (!sequenceStorageRepository.isSessionValid(sessionTopic)) return
        val session = sequenceStorageRepository.getSessionByTopic(sessionTopic)

        when (val response = wcResponse.response) {
            is JsonRpcResponseVO.JsonRpcResult -> {
                Logger.log("Session update methods response received")
                scope.launch { _sequenceEvent.emit(EngineDO.SessionUpdateMethodsResponse.Result(session.topic, session.methods)) }
            }
            is JsonRpcResponseVO.JsonRpcError -> {
                Logger.error("Peer failed to update session: ${response.error}")
                scope.launch { _sequenceEvent.emit(EngineDO.SessionUpdateMethodsResponse.Error(response.errorMessage)) }
            }
        }
    }

    private fun onSessionUpdateEventsResponse(wcResponse: WCResponseVO) {
        val sessionTopic = wcResponse.topic
        if (!sequenceStorageRepository.isSessionValid(sessionTopic)) return
        val session = sequenceStorageRepository.getSessionByTopic(sessionTopic)

        when (val response = wcResponse.response) {
            is JsonRpcResponseVO.JsonRpcResult -> {
                Logger.log("Session update events response received")
                scope.launch {
                    _sequenceEvent.emit(EngineDO.SessionUpdateEventsResponse.Result(session.topic, session.events))
                }
            }
            is JsonRpcResponseVO.JsonRpcError -> {
                Logger.error("Peer failed to events session: ${response.error}")
                scope.launch { _sequenceEvent.emit(EngineDO.SessionUpdateMethodsResponse.Error(response.errorMessage)) }
            }
        }
    }

    private fun onSessionRequestResponse(response: WCResponseVO, params: SessionParamsVO.SessionRequestParams) {
        val result = when (response.response) {
            is JsonRpcResponseVO.JsonRpcResult -> response.response.toEngineJsonRpcResult()
            is JsonRpcResponseVO.JsonRpcError -> response.response.toEngineJsonRpcError()
        }
        val method = params.request.method
        scope.launch { _sequenceEvent.emit(EngineDO.SessionPayloadResponse(response.topic.value, params.chainId, method, result)) }
    }

    private fun resubscribeToSettledSequences() {
        relayer.isConnectionOpened
            .filter { isConnected: Boolean -> isConnected }
            .onEach {
                coroutineScope {
                    launch(Dispatchers.IO) { resubscribeToSettledPairings() }
                    launch(Dispatchers.IO) { resubscribeToSettledSession() }
                }
            }.launchIn(scope)
    }

    private fun resubscribeToSettledPairings() {
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

    private fun resubscribeToSettledSession() {
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
            .filter { session -> session.isAcknowledged }
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