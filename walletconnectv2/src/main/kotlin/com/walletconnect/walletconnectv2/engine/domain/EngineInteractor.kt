package com.walletconnect.walletconnectv2.engine.domain

import com.walletconnect.walletconnectv2.core.exceptions.client.*
import com.walletconnect.walletconnectv2.core.exceptions.peer.PeerError
import com.walletconnect.walletconnectv2.core.model.type.SequenceLifecycle
import com.walletconnect.walletconnectv2.core.model.type.enums.Sequences
import com.walletconnect.walletconnectv2.core.model.vo.ExpiryVO
import com.walletconnect.walletconnectv2.core.model.vo.PublicKey
import com.walletconnect.walletconnectv2.core.model.vo.SecretKey
import com.walletconnect.walletconnectv2.core.model.vo.TopicVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.common.ReasonVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.common.RelayProtocolOptionsVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.common.SessionParticipantVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.pairing.PairingSettlementVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.pairing.params.PairingParamsVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.SessionSettlementVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.params.SessionParamsVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.payload.EventVO
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
        permissions: EngineDO.SessionPermissions,
        blockchain: EngineDO.Blockchain,
        pairingTopic: String?,
        onProposedSequence: (EngineDO.ProposedSequence) -> Unit,
        onFailure: (Throwable) -> Unit,
    ) {

        Validator.validateMethods(permissions.jsonRpc) { errorMessage ->
            throw WalletConnectException.InvalidSessionMethodsException(errorMessage)
        }

        Validator.validateEvents(permissions.events) { errorMessage ->
            throw WalletConnectException.InvalidSessionEventsException(errorMessage)
        }

        Validator.validateBlockchain(blockchain) { errorMessage ->
            throw WalletConnectException.InvalidSessionChainIdsException(errorMessage)
        }

        if (pairingTopic != null) {
            if (!sequenceStorageRepository.isPairingValid(TopicVO(pairingTopic))) {
                throw WalletConnectException.CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE$pairingTopic")
            }
            val pairing: PairingVO = sequenceStorageRepository.getPairingByTopic(TopicVO(pairingTopic))
            val relay = RelayProtocolOptionsVO(pairing.relayProtocol, pairing.relayData)

            proposeSession(permissions, blockchain, TopicVO(pairingTopic), relay,
                onSuccess = { onProposedSequence(EngineDO.ProposedSequence.Session) },
                onFailure = { error -> onFailure(error) })

        } else {
            proposePairing(permissions, blockchain,
                onSessionProposeSuccess = { pairing -> onProposedSequence(pairing) },
                onFailure = { error -> onFailure(error) })
        }
    }

    private fun proposePairing(
        permissions: EngineDO.SessionPermissions,
        blockchain: EngineDO.Blockchain,
        onSessionProposeSuccess: (EngineDO.ProposedSequence.Pairing) -> Unit,
        onFailure: (Throwable) -> Unit,
    ) {
        val pairingTopic: TopicVO = generateTopic()
        val symmetricKey: SecretKey = crypto.generateSymmetricKey(pairingTopic)
        val relay = RelayProtocolOptionsVO()
        val walletConnectUri = EngineDO.WalletConnectUri(pairingTopic, symmetricKey, relay)
        val pairing = PairingVO.createPairing(pairingTopic, relay, walletConnectUri.toAbsoluteString(), metaData)
        sequenceStorageRepository.insertPairing(pairing)
        relayer.subscribe(pairingTopic)

        proposeSession(permissions, blockchain, pairingTopic, relay,
            onSuccess = { onSessionProposeSuccess(EngineDO.ProposedSequence.Pairing(walletConnectUri.toAbsoluteString())) },
            onFailure = { error -> onFailure(error) })
    }

    private fun proposeSession(
        permissions: EngineDO.SessionPermissions,
        blockchain: EngineDO.Blockchain,
        pairingTopic: TopicVO,
        relay: RelayProtocolOptionsVO,
        onFailure: (Throwable) -> Unit = {},
        onSuccess: () -> Unit = {},
    ) {
        val selfPublicKey: PublicKey = crypto.generateKeyPair()
        val sessionProposal = blockchain.toSessionProposeParams(relay, permissions, selfPublicKey, metaData)
        val request = PairingSettlementVO.SessionPropose(id = generateId(), params = sessionProposal)

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

    internal fun reject(proposal: EngineDO.SessionProposal, reason: String, code: Int, onFailure: (Throwable) -> Unit = {}) {
        val request = sessionProposalRequest[proposal.proposerPublicKey] ?: return
        sessionProposalRequest.remove(proposal.proposerPublicKey)
        relayer.respondWithError(request, PeerError.Error(reason, code), onFailure = { error -> onFailure(error) })
    }

    internal fun approve(proposal: EngineDO.SessionProposal, onFailure: (Throwable) -> Unit) {
        val request = sessionProposalRequest[proposal.proposerPublicKey] ?: return
        sessionProposalRequest.remove(proposal.proposerPublicKey)

        Validator.validateProposalFields(proposal) { errorMessage ->
            throw WalletConnectException.InvalidSessionProposalException(errorMessage)
        }

        Validator.validateCAIP10(proposal.accounts) { errorMessage ->
            throw WalletConnectException.InvalidAccountsException(errorMessage)
        }

        Validator.validateIfChainIdsIncludedInPermission(proposal.accounts, proposal.chains) { errorMessage ->
            throw WalletConnectException.InvalidAccountsException(errorMessage)
        }

        val permissions = proposal.toSessionPermissions()
        Validator.validateMethods(permissions.jsonRpc) { errorMessage ->
            throw WalletConnectException.InvalidSessionMethodsException(errorMessage)
        }

        Validator.validateEvents(permissions.events) { errorMessage ->
            throw WalletConnectException.InvalidSessionEventsException(errorMessage)
        }

        val selfPublicKey: PublicKey = crypto.generateKeyPair()
        val (_, sessionTopic) = crypto.generateTopicAndSharedKey(selfPublicKey, PublicKey(proposal.proposerPublicKey))
        relayer.subscribe(sessionTopic)
        val approvalParams = proposal.toSessionApproveParams(selfPublicKey, metaData.toMetaDataVO())
        relayer.respondWithParams(request, approvalParams)
        sessionSettle(proposal, sessionTopic) { error -> onFailure(error) }
    }

    //todo: add updateAccounts, updateMethods, updateEvents
    internal fun update(topic: String, state: EngineDO.SessionState, onFailure: (Throwable) -> Unit) {
        if (!sequenceStorageRepository.isSessionValid(TopicVO(topic))) {
            throw WalletConnectException.CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE$topic")
        }

        val chains: List<String> = sequenceStorageRepository.getSessionByTopic(TopicVO(topic)).chains
        Validator.validateCAIP10(state.accounts) { errorMessage ->
            throw WalletConnectException.InvalidAccountsException(errorMessage)
        }

        Validator.validateIfChainIdsIncludedInPermission(state.accounts, chains) { errorMessage ->
            throw WalletConnectException.InvalidAccountsException(errorMessage)
        }
        val session = sequenceStorageRepository.getSessionByTopic(TopicVO(topic))
        if (!session.isSelfController) {
            throw WalletConnectException.UnauthorizedPeerException(UNAUTHORIZED_UPDATE_MESSAGE)
        }
        if (!session.isAcknowledged) {
            throw WalletConnectException.NotSettledSessionException("$SESSION_IS_NOT_ACKNOWLEDGED_MESSAGE$topic")
        }

        val params = SessionParamsVO.UpdateAccountsParams(state.accounts)
        val sessionUpdateAccounts: SessionSettlementVO.SessionUpdateAccounts =
            SessionSettlementVO.SessionUpdateAccounts(id = generateId(), params = params)
        sequenceStorageRepository.updateSessionWithAccounts(TopicVO(topic), state.accounts)
        relayer.publishJsonRpcRequests(TopicVO(topic), sessionUpdateAccounts,
            onSuccess = { Logger.log("Session update sent successfully") },
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
            throw WalletConnectException.InvalidNotificationException(errorMessage)
        }

        val session = sequenceStorageRepository.getSessionByTopic(TopicVO(topic))
        Validator.validateEventAuthorization(session, event.name) { errorMessage ->
            throw WalletConnectException.UnauthorizedNotificationException(errorMessage)
        }

        val eventParams = SessionParamsVO.EventParams(EventVO(event.name, event.data), event.chainId)
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

        val deleteParams = SessionParamsVO.DeleteParams(ReasonVO(message = reason, code = code))
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

    private fun sessionSettle(proposal: EngineDO.SessionProposal, sessionTopic: TopicVO, onFailure: (Throwable) -> Unit) {
        val (_, selfPublicKey) = crypto.getKeyAgreement(sessionTopic)
        val selfParticipant = SessionParticipantVO(selfPublicKey.keyAsHex, metaData.toMetaDataVO())
        val sessionExpiry = Expiration.activeSession
        val session = SessionVO.createUnacknowledgedSession(sessionTopic, proposal, selfParticipant, sessionExpiry)
        sequenceStorageRepository.insertSession(session)
        val params = proposal.toSessionSettleParams(selfParticipant, sessionExpiry)
        val sessionSettle = SessionSettlementVO.SessionSettle(id = generateId(), params = params)
        relayer.publishJsonRpcRequests(sessionTopic, sessionSettle, onFailure = { error -> onFailure(error) })
    }

    private fun onSessionSettle(request: WCRequestVO, settleParams: SessionParamsVO.SessionSettleParams) {
        val sessionTopic = request.topic
        val (_, selfPublicKey) = crypto.getKeyAgreement(sessionTopic)
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
        scope.launch { _sequenceEvent.emit(EngineDO.DeletedPairing(request.topic.value, params.reason.message)) }
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
        scope.launch { _sequenceEvent.emit(params.toEngineDOSessionRequest(request)) }
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

        //todo: emit SessionUpdateAccounts
        scope.launch { _sequenceEvent.emit(EngineDO.SessionUpdate(request.topic, params.accounts)) }
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

        val jsonRpcMethods = EngineDO.SessionPermissions.JsonRpc(params.methods)
        Validator.validateMethods(jsonRpcMethods) {
            relayer.respondWithError(request, PeerError.InvalidUpdateMethodsRequest(Sequences.SESSION.name))
            return@validateMethods
        }

        sequenceStorageRepository.updateSessionWithMethods(session.topic, params.methods)
        relayer.respondWithSuccess(request)

        //todo: emit SessionUpdateMethods
        scope.launch { _sequenceEvent.emit(EngineDO.SessionUpdate(request.topic, params.methods)) }
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

        val events = EngineDO.SessionPermissions.Events(params.events)
        Validator.validateEvents(events) {
            relayer.respondWithError(request, PeerError.InvalidUpdateEventsRequest(Sequences.SESSION.name))
            return@validateEvents
        }

        sequenceStorageRepository.updateSessionWithEvents(session.topic, params.events)
        relayer.respondWithSuccess(request)

        //todo: emit SessionUpdateEvents
        scope.launch { _sequenceEvent.emit(EngineDO.SessionUpdate(request.topic, params.events)) }
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
        scope.launch { _sequenceEvent.emit(session.toEngineDOExtendedSessionVO(ExpiryVO(newExpiry))) }
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
                val responderPublicKey = PublicKey(approveParams.responder.publicKey)
                val (_, sessionTopic) = crypto.generateTopicAndSharedKey(selfPublicKey, responderPublicKey)
                val peerMetadata = approveParams.responder.metadata
                sequenceStorageRepository.updatePairingPeerMetadata(pairingTopic, peerMetadata)
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
//                todo: emit SessionUpdateMethodsResponse
            }
            is JsonRpcResponseVO.JsonRpcError -> {
                Logger.error("Peer failed to update session: ${response.error}")
//                todo: emit SessionUpdateMethodsResponse
            }
        }
    }

    private fun onSessionUpdateEventsResponse(wcResponse: WCResponseVO) {
        val sessionTopic = wcResponse.topic
        if (!sequenceStorageRepository.isSessionValid(sessionTopic)) return
        val session = sequenceStorageRepository.getSessionByTopic(sessionTopic)

        when (val response = wcResponse.response) {
            is JsonRpcResponseVO.JsonRpcResult -> {
                Logger.log("Session update ecents response received")
//                todo: emit SessionUpdateEventsResponse
            }
            is JsonRpcResponseVO.JsonRpcError -> {
                Logger.error("Peer failed to events session: ${response.error}")
//                todo: emit SessionUpdateEventsResponse
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