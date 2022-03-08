package com.walletconnect.walletconnectv2.engine.domain

import com.walletconnect.walletconnectv2.core.exceptions.client.*
import com.walletconnect.walletconnectv2.core.exceptions.peer.Error
import com.walletconnect.walletconnectv2.core.exceptions.peer.PeerError
import com.walletconnect.walletconnectv2.core.model.type.SequenceLifecycle
import com.walletconnect.walletconnectv2.core.model.type.enums.Sequences
import com.walletconnect.walletconnectv2.core.model.vo.ExpiryVO
import com.walletconnect.walletconnectv2.core.model.vo.PublicKey
import com.walletconnect.walletconnectv2.core.model.vo.SecretKey
import com.walletconnect.walletconnectv2.core.model.vo.TopicVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.common.ReasonVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.common.RelayProtocolOptionsVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.pairing.PairingSettlementVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.pairing.params.PairingParamsVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.pairing.payload.SessionProposerVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.SessionSettlementVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.params.SessionParamsVO
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
import com.walletconnect.walletconnectv2.storage.sequence.SequenceStatus
import com.walletconnect.walletconnectv2.storage.sequence.SequenceStorageRepository
import com.walletconnect.walletconnectv2.util.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

internal class EngineInteractor(
    private val relayer: WalletConnectRelayer,
    private val crypto: CryptoRepository,
    private val sequenceStorageRepository: SequenceStorageRepository,
    private val metaData: EngineDO.AppMetaData
) {
    private val _sequenceEvent: MutableSharedFlow<SequenceLifecycle> = MutableSharedFlow()
    val sequenceEvent: SharedFlow<SequenceLifecycle> = _sequenceEvent

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
        onFailure: (Throwable) -> Unit
    ): String? {

        Validator.validatePermissions(permissions.jsonRpc, permissions.notifications) { errorMessage ->
            throw WalletConnectException.InvalidSessionPermissionsException(errorMessage)
        }

        Validator.validateBlockchain(blockchain) { errorMessage ->
            throw WalletConnectException.InvalidSessionChainIdsException(errorMessage)
        }

        if (pairingTopic != null) {
            if (!isSequenceValid(TopicVO(pairingTopic))) {
                throw WalletConnectException.CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE$pairingTopic")
            }
            proposeSession(permissions, blockchain, pairingTopic) { error -> onFailure(error) }
            return null
        }
        return proposePairing()
    }

    private fun proposePairing(): String {
        val topic: TopicVO = generateTopic()
        val symmetricKey: SecretKey = crypto.generateSymmetricKey(topic)
        val relay = RelayProtocolOptionsVO()
        val walletConnectUri = EngineDO.WalletConnectUri(topic, symmetricKey, relay)
        val pairing = createPairing(topic, relay, walletConnectUri.toAbsoluteString())
        //todo: setting inactive pairing - 5mins
        sequenceStorageRepository.insertPairing(pairing)
        relayer.subscribe(topic)
        //todo: send wc_sessionPropose on topicA
        return walletConnectUri.toAbsoluteString()
    }

    private fun proposeSession(
        permissions: EngineDO.SessionPermissions,
        blockchain: EngineDO.Blockchain,
        pairingTopic: String,
        onFailure: (Throwable) -> Unit = {}
    ) {
        val settledPairing: PairingVO = sequenceStorageRepository.getPairingByTopic(TopicVO(pairingTopic))

        if (settledPairing.status == SequenceStatus.ACKNOWLEDGED) {

            val pendingSessionTopic: TopicVO = generateTopic()
            val selfPublicKey: PublicKey = crypto.generateKeyPair()

            val proposalParams: PairingParamsVO.SessionProposeParams = SessionProposerVO(selfPublicKey.keyAsHex, metaData.toMetaDataVO())
                .toProposalParams(pendingSessionTopic, settledPairing.topic, permissions, blockchain)

            val proposedSession: SessionVO = proposalParams.toProposedSessionVO(pendingSessionTopic, selfPublicKey, blockchain)

            sequenceStorageRepository.insertPendingSession(proposedSession, metaData.toMetaDataVO())
            relayer.subscribe(pendingSessionTopic)

            val (secretKey, publicKey) = crypto.getKeyAgreement(settledPairing.topic)
            crypto.setEncryptionKeys(secretKey, publicKey, pendingSessionTopic)

            //todo: change as the last part of refactor
//            val params = PairingParamsVO.PayloadParams(ProposalRequestVO(JsonRpcMethod.WC_SESSION_PROPOSE, params = proposalParams))
//            val sessionProposal = SettlementPairingVO.PairingPayload(id = generateId(), params = params)
//            relayer.publishJsonRpcRequests(settledPairing.topic, sessionProposal, prompt,
//                onSuccess = { Logger.log("Session proposal sent successfully") },
//                onFailure = { error ->
//                    Logger.error("Failed to send a session proposal: $error")
//                    onFailure(error)
//                }
//            )
        }
    }

    internal fun pair(uri: String) {
        val walletConnectUri: EngineDO.WalletConnectUri =
            Validator.validateWCUri(uri) ?: throw WalletConnectException.MalformedWalletConnectUri(MALFORMED_PAIRING_URI_MESSAGE)
        if (sequenceStorageRepository.isPairingValid(walletConnectUri.topic)) {
            throw WalletConnectException.PairWithExistingPairingIsNotAllowed(PAIRING_NOW_ALLOWED_MESSAGE)
        }

        val pairing = walletConnectUri.toPairingVO()
        val symmetricKey = walletConnectUri.symKey
        crypto.setSymmetricKey(walletConnectUri.topic, symmetricKey)
        //todo: setting inactive pairing - 5mins
        sequenceStorageRepository.insertPairing(pairing)
        relayer.subscribe(pairing.topic)
    }

    //todo: send as success response to wc_sessionApprove
    internal fun approve(proposal: EngineDO.SessionProposal, onFailure: (Throwable) -> Unit) {
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
        Validator.validatePermissions(permissions.jsonRpc, permissions.notifications) { errorMessage ->
            throw WalletConnectException.InvalidSessionPermissionsException(errorMessage)
        }

        val selfPublicKey: PublicKey = crypto.generateKeyPair()
        val (_, settledTopic) = crypto.generateTopicAndSharedKey(selfPublicKey, PublicKey(proposal.publicKey))

        val pendingSession = proposal.toRespondedSessionVO(selfPublicKey, settledTopic)
        val settledSession = proposal.toPreSettledSessionVO(settledTopic, selfPublicKey)

        sequenceStorageRepository.insertPendingSession(pendingSession, metaData.toMetaDataVO())
        relayer.subscribe(TopicVO(proposal.topic))

        sequenceStorageRepository.insertSettledSession(settledSession, metaData.toMetaDataVO())
        relayer.subscribe(settledTopic)

//        val params = SessionParamsVO.ApprovalParams(
//            relay = RelayProtocolOptionsVO(),
//            state = SessionStateVO(proposal.accounts),
//            expiry = settledSession.expiry,
//            responder = SessionParticipantVO(selfPublicKey.keyAsHex, metadata = metaData.toMetaDataVO())
//        )

        //todo: send wc_sessionSettle request
        //todo: sent wc_pairingExtend from 5mins to 30days
//        val sessionApprove = PreSettlementSessionVO.Approve(id = generateId(), params = params)
//        relayer.publishJsonRpcRequests(TopicVO(proposal.topic), sessionApprove,
//            onSuccess = { Logger.log("Session approval sent successfully") },
//            onFailure = { error ->
//                Logger.error("Sending session approve error: $error")
//                onFailure(error)
//            }
//        )
    }

    //todo: send as error response to wc_sessionApprove
    internal fun reject(reason: String, topic: String, onFailure: (Throwable) -> Unit) {
//        val params = SessionParamsVO.RejectParams(reason = ReasonVO(message = reason))
//        val sessionReject = PreSettlementSessionVO.Reject(id = generateId(), params = params)
//
//        //todo: sent wc_pairingExtend from 5mins to 30days
//        relayer.publishJsonRpcRequests(TopicVO(topic), sessionReject,
//            onSuccess = { Logger.log("Session reject sent successfully") },
//            onFailure = { error ->
//                Logger.error("Sending session reject error: $error")
//                onFailure(error)
//            }
//        )
    }

    internal fun upgrade(topic: String, permissions: EngineDO.SessionPermissions, onFailure: (Throwable) -> Unit) {
        if (!isSequenceValid(TopicVO(topic))) {
            throw WalletConnectException.CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE$topic")
        }

        Validator.validatePermissions(permissions.jsonRpc, permissions.notifications) { errorMessage ->
            throw WalletConnectException.InvalidSessionPermissionsException(errorMessage)
        }

        val session = sequenceStorageRepository.getSessionByTopic(TopicVO(topic))
        if (!session.isSelfController) {
            throw WalletConnectException.UnauthorizedPeerException(UNAUTHORIZED_UPGRADE_MESSAGE)
        }

        if (session.status != SequenceStatus.ACKNOWLEDGED) {
            throw WalletConnectException.NotSettledSessionException("$SESSION_IS_NOT_SETTLED_MESSAGE$topic")
        }

        val upgradePermissionsParams = SessionParamsVO.UpgradeParams(permissions = permissions.toSessionsPermissions())
        val sessionUpgrade = SessionSettlementVO.SessionUpgrade(id = generateId(), params = upgradePermissionsParams)
        sequenceStorageRepository.upgradeSessionWithPermissions(
            TopicVO(topic),
            permissions.notifications?.types,
            permissions.jsonRpc.methods
        )

        relayer.publishJsonRpcRequests(
            TopicVO(topic), sessionUpgrade,
            onSuccess = { Logger.log("Session upgrade sent successfully") },
            onFailure = { error ->
                Logger.error("Sending session upgrade error: $error")
                onFailure(error)
            }
        )
    }

    internal fun update(topic: String, state: EngineDO.SessionState, onFailure: (Throwable) -> Unit) {
        if (!isSequenceValid(TopicVO(topic))) {
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
        if (session.status != SequenceStatus.ACKNOWLEDGED) {
            throw WalletConnectException.NotSettledSessionException("$SESSION_IS_NOT_SETTLED_MESSAGE$topic")
        }

        //TODO: get chains from user or split account and get from account
//        val params = SessionParamsVO.UpdateParams(BlockchainSettledVO(state.accounts))
//        val sessionUpdate: SessionSettlementVO.SessionUpdate = SessionSettlementVO.SessionUpdate(id = generateId(), params = params)
//        sequenceStorageRepository.updateSessionWithAccounts(TopicVO(topic), state.accounts)

//        relayer.publishJsonRpcRequests(
//            TopicVO(topic), sessionUpdate,
//            onSuccess = { Logger.log("Session update sent successfully") },
//            onFailure = { error ->
//                Logger.error("Sending session update error: $error")
//                onFailure(error)
//            }
//        )
    }

    internal fun sessionRequest(request: EngineDO.Request, onFailure: (Throwable) -> Unit) {
        if (!isSequenceValid(TopicVO(request.topic))) {
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
            TopicVO(request.topic), sessionPayload, prompt,
            onSuccess = {
                Logger.log("Session request sent successfully")
                scope.launch {
                    try {
                        withTimeout(FIVE_MINUTES_TIMEOUT) {
                            collectResponse(sessionPayload.id) {
                                cancel()
                            }
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

    internal fun respondSessionPayload(topic: String, jsonRpcResponse: JsonRpcResponseVO, onFailure: (Throwable) -> Unit) {
        if (!isSequenceValid(TopicVO(topic))) {
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

    internal fun notify(topic: String, notification: EngineDO.Notification, onFailure: (Throwable) -> Unit) {
        if (!isSequenceValid(TopicVO(topic))) {
            throw WalletConnectException.CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE$topic")
        }

        Validator.validateNotification(notification) { errorMessage ->
            throw WalletConnectException.InvalidNotificationException(errorMessage)
        }

        val session = sequenceStorageRepository.getSessionByTopic(TopicVO(topic))
        Validator.validateNotificationAuthorization(session, notification.type) { errorMessage ->
            throw WalletConnectException.UnauthorizedNotificationException(errorMessage)
        }

        val notificationParams = SessionParamsVO.NotifyParams(notification.type, notification.data)
        val sessionNotification = SessionSettlementVO.SessionNotify(id = generateId(), params = notificationParams)

        relayer.publishJsonRpcRequests(TopicVO(topic), sessionNotification,
            onSuccess = { Logger.log("Notify sent successfully") },
            onFailure = { error ->
                Logger.error("Sending notify error: $error")
                onFailure(error)
            }
        )
    }

    fun sessionExtend(topic: String, ttl: Long, onFailure: (Throwable) -> Unit) {
        if (!isSequenceValid(TopicVO(topic))) {
            throw WalletConnectException.CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE$topic")
        }

        val session = sequenceStorageRepository.getSessionByTopic(TopicVO(topic))
        if (!session.isSelfController) {
            throw WalletConnectException.UnauthorizedPeerException(UNAUTHORIZED_EXTEND_MESSAGE)
        }
        if (session.status != SequenceStatus.ACKNOWLEDGED) {
            throw WalletConnectException.NotSettledSessionException("$SESSION_IS_NOT_SETTLED_MESSAGE$topic")
        }

        Validator.validateSessionExtend(ttl, session.expiry.seconds) { errorMessage ->
            throw WalletConnectException.InvalidExtendException(errorMessage)
        }

        sequenceStorageRepository.updateSessionExpiry(TopicVO(topic), ttl)
        val sessionExtend = SessionSettlementVO.SessionExtend(id = generateId(), params = SessionParamsVO.ExtendParams(ttl = ttl))
        relayer.publishJsonRpcRequests(TopicVO(topic), sessionExtend,
            onSuccess = { Logger.error("Session extend sent successfully") },
            onFailure = { error ->
                Logger.error("Sending session extend error: $error")
                onFailure(error)
            })
    }

    private fun pairingExtend(topic: String, ttl: Long) {
        if (!isSequenceValid(TopicVO(topic))) {
            throw WalletConnectException.CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE$topic")
        }

        val pairing = sequenceStorageRepository.getPairingByTopic(TopicVO(topic))
        Validator.validatePairingExtend(ttl, pairing.expiry.seconds) { errorMessage ->
            throw WalletConnectException.InvalidExtendException(errorMessage)
        }

        sequenceStorageRepository.updatePairingExpiry(TopicVO(topic), ttl)
        val pairingExtend = PairingSettlementVO.PairingExtend(id = generateId(), params = PairingParamsVO.ExtendParams(ttl = ttl))
        relayer.publishJsonRpcRequests(TopicVO(topic), pairingExtend,
            onSuccess = { Logger.error("Pairing extend sent successfully") },
            onFailure = { error -> Logger.error("Pairing session extend error: $error") })
    }

    internal fun disconnect(topic: String, reason: String, code: Int) {
        if (!isSequenceValid(TopicVO(topic))) {
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

    private suspend fun collectResponse(id: Long, onResponse: (Result<JsonRpcResponseVO.JsonRpcResult>) -> Unit = {}) {
        relayer.peerResponse
            .filter { response -> response.result.id == id }
            .collect { response ->
                when (val result = response.result) {
                    is JsonRpcResponseVO.JsonRpcResult -> onResponse(Result.success(result))
                    is JsonRpcResponseVO.JsonRpcError -> onResponse(Result.failure(Throwable(result.errorMessage)))
                }
            }
    }

    internal fun getListOfSettledSessions(): List<EngineDO.Session> {
        return sequenceStorageRepository.getListOfSessionVOs()
            .filter { session -> session.status == SequenceStatus.ACKNOWLEDGED && session.expiry.isSequenceValid() }
            .map { session -> session.toEngineDOSettledSessionVO() }
    }

    internal fun getListOfSettledPairings(): List<EngineDO.PairingSettle> {
        return sequenceStorageRepository.getListOfPairingVOs()
            .filter { pairing -> pairing.status == SequenceStatus.ACKNOWLEDGED && pairing.expiry.isSequenceValid() }
            .map { pairing -> pairing.toEngineDOSettledPairing() }
    }

    internal fun getPendingRequests(topic: TopicVO): List<PendingRequestVO> = relayer.getPendingRequests(topic)

    private fun collectJsonRpcRequests() {
        scope.launch {
            relayer.clientSyncJsonRpc.collect { request ->
                when (val requestParams = request.params) {
                    //todo: add wc_sessionPropose without payload, wc_sessionSettle
                    is PairingParamsVO.SessionProposeParams -> onPairingPayload(request, requestParams)
                    is PairingParamsVO.DeleteParams -> onPairingDelete(request, requestParams)
                    is PairingParamsVO.ExtendParams -> onPairingExtend(request, requestParams)

//                    is SessionParamsVO.ApprovalParams -> onSessionApprove(request, requestParams)
//                    is SessionParamsVO.RejectParams -> onSessionReject(request, requestParams)
                    is SessionParamsVO.DeleteParams -> onSessionDelete(request, requestParams)
                    is SessionParamsVO.SessionRequestParams -> onSessionPayload(request, requestParams)
                    is SessionParamsVO.UpdateParams -> onSessionUpdate(request, requestParams)
                    is SessionParamsVO.UpgradeParams -> onSessionUpgrade(request, requestParams)
                    is SessionParamsVO.NotifyParams -> onSessionNotification(request, requestParams)
                    is SessionParamsVO.ExtendParams -> onSessionExtend(request, requestParams)
                    is SessionParamsVO.PingParams, is PairingParamsVO.PingParams -> onPing(request)
                }
            }
        }
    }

    private fun onSessionExtend(request: WCRequestVO, requestParams: SessionParamsVO.ExtendParams) {
        if (!isSequenceValid(request.topic)) {
            relayer.respondWithError(request, PeerError(Error.NoMatchingTopic(Sequences.SESSION.name, request.topic.value)))
            return
        }

        val session = sequenceStorageRepository.getSessionByTopic(request.topic)
        if (!session.isSelfController) {
            relayer.respondWithError(request, PeerError(Error.UnauthorizedExtendRequest(Sequences.SESSION.name)))
            return
        }

        val ttl = requestParams.ttl
        Validator.validateSessionExtend(ttl, session.expiry.seconds) {
            relayer.respondWithError(request, PeerError(Error.UnauthorizedExtendRequest(Sequences.SESSION.name)))
            return@validateSessionExtend
        }

        sequenceStorageRepository.updateSessionExpiry(request.topic, ttl)
        relayer.respondWithSuccess(request)
        scope.launch { _sequenceEvent.emit(session.toEngineDOExtendedSessionVO(ExpiryVO(ttl))) }
    }

    private fun onPairingExtend(request: WCRequestVO, requestParams: PairingParamsVO.ExtendParams) {
        if (!isSequenceValid(request.topic)) {
            relayer.respondWithError(request, PeerError(Error.NoMatchingTopic(Sequences.PAIRING.name, request.topic.value)))
            return
        }

        val pairing = sequenceStorageRepository.getPairingByTopic(request.topic)
        val ttl = requestParams.ttl
        Validator.validateSessionExtend(ttl, pairing.expiry.seconds) {
            relayer.respondWithError(request, PeerError(Error.UnauthorizedExtendRequest(Sequences.SESSION.name)))
            return@validateSessionExtend
        }

        sequenceStorageRepository.updatePairingExpiry(request.topic, ttl)
        relayer.respondWithSuccess(request)
    }

    //todo: change to wc_sessionPropose
    private fun onPairingPayload(request: WCRequestVO, payloadParams: PairingParamsVO.SessionProposeParams) {
        if (!isSequenceValid(request.topic)) {
            relayer.respondWithError(request, PeerError(Error.NoMatchingTopic(Sequences.PAIRING.name, request.topic.value)))
            return
        }

//        if (payloadParams.request.method != JsonRpcMethod.WC_SESSION_PROPOSE) {
//            relayer.respondWithError(request, PeerError(Error.UnauthorizedJsonRpcMethod(request.method)))
//            return
//        }

//        val proposal: SessionParamsVO.ProposalParams = payloadParams.request.params
//        val session = sequenceStorageRepository.getSessionByTopic(request.topic)
//        if (proposal.proposer.controller == session.isSelfController) {
//            relayer.respondWithError(request, PeerError(Error.UnauthorizedMatchingController(session.isSelfController)))
//            return
//        }
//
//        val (secretKey, publicKey) = crypto.getKeyAgreement(proposal.signal.params.topic)
//        crypto.setEncryptionKeys(secretKey, publicKey, proposal.topic)

        relayer.respondWithSuccess(request)
        scope.launch { _sequenceEvent.emit(payloadParams.toEngineDOSessionProposal()) }
    }

    private fun onPairingDelete(request: WCRequestVO, params: PairingParamsVO.DeleteParams) {
        if (!isSequenceValid(request.topic)) {
            relayer.respondWithError(request, PeerError(Error.NoMatchingTopic(Sequences.PAIRING.name, request.topic.value)))
            return
        }

        crypto.removeKeys(request.topic.value)
        relayer.unsubscribe(request.topic)
        sequenceStorageRepository.deletePairing(request.topic)
        scope.launch { _sequenceEvent.emit(EngineDO.DeletedPairing(request.topic.value, params.reason.message)) }
    }

//    private fun onSessionApprove(request: WCRequestVO, params: SessionParamsVO.ApprovalParams) {
//        if (!isSequenceValid(request.topic)) {
//            relayer.respondWithError(request, PeerError(Error.NoMatchingTopic(Sequences.SESSION.name, request.topic.value)))
//            return
//        }
//
//        val pendingSession: SessionVO = sequenceStorageRepository.getSessionByTopic(request.topic)
//        if (pendingSession.isSelfController) {
//            relayer.respondWithError(request, PeerError(Error.UnauthorizedMatchingController(true)))
//            return
//        }
//
//        if (pendingSession.status != SequenceStatus.PROPOSED) {
//            relayer.respondWithError(request, PeerError(Error.NoMatchingTopic(Sequences.SESSION.name, request.topic.value)))
//            return
//        }
//
//        val (_, settledTopic) = crypto.generateTopicAndSharedKey(pendingSession.selfParticipant, PublicKey(params.responder.publicKey))
//
//        val settledSession = pendingSession.toEngineDOAcknowledgeSessionVO(settledTopic, params)
//
//        sequenceStorageRepository.deleteSession(pendingSession.topic)
//        sequenceStorageRepository.insertSettledSession(settledSession, metaData.toMetaDataVO())
//        relayer.subscribe(settledTopic)
//        relayer.unsubscribe(pendingSession.topic)
//
//        relayer.respondWithSuccess(request)
//        scope.launch { _sequenceEvent.emit(pendingSession.toSessionApproved(params, settledTopic)) }
//    }

//    private fun onSessionReject(request: WCRequestVO, params: SessionParamsVO.RejectParams) {
//        if (!isSequenceValid(request.topic)) {
//            relayer.respondWithError(request, PeerError(Error.NoMatchingTopic(Sequences.SESSION.name, request.topic.value)))
//            return
//        }
//
//        sequenceStorageRepository.deleteSession(request.topic)
//        relayer.unsubscribe(request.topic)
//        relayer.respondWithSuccess(request)
//        scope.launch { _sequenceEvent.emit(EngineDO.SessionRejected(request.topic.value, params.reason.message)) }
//    }

    private fun onSessionDelete(request: WCRequestVO, params: SessionParamsVO.DeleteParams) {
        if (!isSequenceValid(request.topic)) {
            relayer.respondWithError(request, PeerError(Error.NoMatchingTopic(Sequences.SESSION.name, request.topic.value)))
            return
        }

        crypto.removeKeys(request.topic.value)
        sequenceStorageRepository.deleteSession(request.topic)
        relayer.unsubscribe(request.topic)
        relayer.respondWithSuccess(request)
        scope.launch { _sequenceEvent.emit(params.toEngineDoDeleteSession(request.topic)) }
    }

    private fun onSessionPayload(request: WCRequestVO, params: SessionParamsVO.SessionRequestParams) {
        if (!isSequenceValid(request.topic)) {
            relayer.respondWithError(request, PeerError(Error.NoMatchingTopic(Sequences.SESSION.name, request.topic.value)))
            return
        }

        val session = sequenceStorageRepository.getSessionByTopic(request.topic)
        if (params.chainId != null && !session.chains.contains(params.chainId)) {
            relayer.respondWithError(request, PeerError(Error.UnauthorizedTargetChainId(params.chainId)))
            return
        }

        val method = params.request.method
        if (!session.methods.contains(method)) {
            relayer.respondWithError(request, PeerError(Error.UnauthorizedJsonRpcMethod(method)))
            return
        }
        scope.launch { _sequenceEvent.emit(params.toEngineDOSessionRequest(request)) }
    }

    private fun onSessionUpdate(request: WCRequestVO, params: SessionParamsVO.UpdateParams) {
        if (!isSequenceValid(request.topic)) {
            relayer.respondWithError(request, PeerError(Error.NoMatchingTopic(Sequences.SESSION.name, request.topic.value)))
            return
        }

        val session: SessionVO = sequenceStorageRepository.getSessionByTopic(request.topic)
        if (!session.isPeerController) {
            relayer.respondWithError(request, PeerError(Error.UnauthorizedUpdateRequest(Sequences.SESSION.name)))
            return
        }

        Validator.validateCAIP10(params.blockchain.accounts) {
            relayer.respondWithError(request, PeerError(Error.InvalidUpdateRequest(Sequences.SESSION.name)))
            return@validateCAIP10
        }

        sequenceStorageRepository.updateSessionWithAccounts(session.topic, params.blockchain.accounts)
        relayer.respondWithSuccess(request)
        scope.launch { _sequenceEvent.emit(EngineDO.SessionUpdate(request.topic, params.blockchain.accounts)) }
    }

    private fun onSessionUpgrade(request: WCRequestVO, params: SessionParamsVO.UpgradeParams) {
        if (!isSequenceValid(request.topic)) {
            relayer.respondWithError(request, PeerError(Error.NoMatchingTopic(Sequences.SESSION.name, request.topic.value)))
            return
        }

        val session: SessionVO = sequenceStorageRepository.getSessionByTopic(request.topic)
        if (!session.isPeerController) {
            relayer.respondWithError(request, PeerError(Error.UnauthorizedUpgradeRequest(Sequences.SESSION.name)))
            return
        }

        val permissions = params.permissions.toEngineDOPermissions()
        Validator.validatePermissions(permissions.jsonRpc, permissions.notifications) {
            relayer.respondWithError(request, PeerError(Error.InvalidUpgradeRequest(Sequences.SESSION.name)))
            return@validatePermissions
        }

        val notificationTypes = params.permissions.notifications?.types ?: listOf()
        val methods = params.permissions.jsonRpc.methods
        sequenceStorageRepository.upgradeSessionWithPermissions(request.topic, notificationTypes, methods)
        relayer.respondWithSuccess(request)

        val typesUnion = session.chains.union(notificationTypes).toList()
        val methodsUnion = session.methods.union(methods).toList()
        scope.launch { _sequenceEvent.emit(EngineDO.SessionUpgrade(session.topic, typesUnion, methodsUnion)) }
    }

    private fun onSessionNotification(request: WCRequestVO, params: SessionParamsVO.NotifyParams) {
        if (!isSequenceValid(request.topic)) {
            relayer.respondWithError(request, PeerError(Error.NoMatchingTopic(Sequences.SESSION.name, request.topic.value)))
            return
        }

        val session = sequenceStorageRepository.getSessionByTopic(request.topic)
        if (session.status != SequenceStatus.ACKNOWLEDGED) {
            relayer.respondWithError(request, PeerError(Error.NoMatchingTopic(Sequences.SESSION.name, request.topic.value)))
            return
        }

        Validator.validateNotificationAuthorization(session, params.type) {
            relayer.respondWithError(request, PeerError(Error.UnauthorizedNotificationType(params.type)))
            return@validateNotificationAuthorization
        }

        relayer.respondWithSuccess(request)
        scope.launch { _sequenceEvent.emit(params.toEngineDoSessionNotification(request.topic)) }
    }

    private fun onPing(request: WCRequestVO) {
        if (!isSequenceValid(request.topic)) {
            relayer.respondWithError(request, PeerError(Error.NoMatchingTopic(Sequences.SESSION.name, request.topic.value)))
            return
        }

        relayer.respondWithSuccess(request)
    }

    private fun collectJsonRpcResponses() {
        scope.launch {
            relayer.peerResponse.collect { response ->
                when (val params = response.params) {
                    is PairingParamsVO.SessionProposeParams -> onSessionProposalResponse(response, params)
//                    is SessionParamsVO.ApprovalParams -> onSessionApproveResponse(response)
                    is SessionParamsVO.UpgradeParams -> onSessionUpgradeResponse(response)
                    is SessionParamsVO.UpdateParams -> onSessionUpdateResponse(response)
                    is SessionParamsVO.SessionRequestParams -> onSessionRequestResponse(response, params)
                }
            }
        }
    }


    private fun onSessionProposalResponse(response: WCResponseVO, params: PairingParamsVO.SessionProposeParams) {
        when (val result = response.result) {
            is JsonRpcResponseVO.JsonRpcResult -> Logger.log("Session proposal response received")
            is JsonRpcResponseVO.JsonRpcError -> {
                Logger.log("Session proposal error response received: ${result.error}")
//                with(params.request.params) {
//                    relayer.unsubscribe(response.topic)
//                    sequenceStorageRepository.deleteSession(topic)
//                    crypto.removeKeys(topic.value)
//                }
            }
        }
    }

//    private fun onSessionApproveResponse(response: WCResponseVO) {
//        val pendingTopic = response.topic
//        if (!isSequenceValid(pendingTopic)) return //topic C
//        val pendingSession: SessionVO = sequenceStorageRepository.getSessionByTopic(pendingTopic)
//        val settledTopic = pendingSession.outcomeTopic //topic D
//        if (settledTopic.value.isEmpty()) return
//
//        when (val result = response.result) {
//            is JsonRpcResponseVO.JsonRpcResult -> {
//                Logger.log("Session approve response received")
//                if (!isSequenceValid(settledTopic)) return
//                val settledSession = sequenceStorageRepository.getSessionByTopic(settledTopic)
//                relayer.unsubscribe(pendingTopic)
//                sequenceStorageRepository.deleteSession(pendingTopic)
//                crypto.removeKeys(pendingTopic.value)
//                sequenceStorageRepository.updatePreSettledSessionToAcknowledged(settledSession.copy(status = SequenceStatus.ACKNOWLEDGED))
//                val expiry = settledSession.expiry
//                scope.launch {
//                    _sequenceEvent.emit(
//                        EngineDO.SettledSessionResponse.Result(settledSession.toEngineDOSettledSessionVO(settledTopic, expiry))
//                    )
//                }
//            }
//            is JsonRpcResponseVO.JsonRpcError -> {
//                Logger.error("Session approval error response received: ${result.error}")
//                relayer.unsubscribe(pendingTopic)
//                relayer.unsubscribe(settledTopic)
//                crypto.removeKeys(pendingSession.topic.value)
//                crypto.removeKeys(settledTopic.value)
//                sequenceStorageRepository.deleteSession(settledTopic)
//                sequenceStorageRepository.deleteSession(pendingTopic)
//                scope.launch { _sequenceEvent.emit(EngineDO.SettledSessionResponse.Error(result.errorMessage)) }
//            }
//        }
//    }

    private fun onSessionUpgradeResponse(response: WCResponseVO) {
        if (!isSequenceValid(response.topic)) return
        val session = sequenceStorageRepository.getSessionByTopic(response.topic)

        when (val result = response.result) {
            is JsonRpcResponseVO.JsonRpcResult -> {
                Logger.log("Session upgrade response received")
                scope.launch { _sequenceEvent.emit(EngineDO.SessionUpgradeResponse.Result(session.topic, session.chains, session.methods)) }
            }
            is JsonRpcResponseVO.JsonRpcError -> {
                Logger.error("Peer failed to upgrade session: ${result.error}")
                scope.launch { _sequenceEvent.emit(EngineDO.SessionUpgradeResponse.Error(result.errorMessage)) }
            }
        }
    }

    private fun onSessionUpdateResponse(response: WCResponseVO) {
        if (!isSequenceValid(response.topic)) return
        val session = sequenceStorageRepository.getSessionByTopic(response.topic)

        when (val result = response.result) {
            is JsonRpcResponseVO.JsonRpcResult -> {
                Logger.log("Session update response received")
                scope.launch { _sequenceEvent.emit(EngineDO.SessionUpdateResponse.Result(session.topic, session.accounts)) }
            }
            is JsonRpcResponseVO.JsonRpcError -> {
                Logger.error("Peer failed to update session: ${result.error}")
                scope.launch { _sequenceEvent.emit(EngineDO.SessionUpdateResponse.Error(result.errorMessage)) }
            }
        }
    }

    private fun onSessionRequestResponse(response: WCResponseVO, params: SessionParamsVO.SessionRequestParams) {
        val result = when (response.result) {
            is JsonRpcResponseVO.JsonRpcResult -> response.result.toEngineJsonRpcResult()
            is JsonRpcResponseVO.JsonRpcError -> response.result.toEngineJsonRpcError()
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
        val (listOfExpiredPairing, listOfValidPairing) = sequenceStorageRepository.getListOfPairingVOs()
            .partition { pairing -> !pairing.expiry.isSequenceValid() }

        listOfExpiredPairing
            .map { pairing -> pairing.topic }
            .onEach { pairingTopic ->
                relayer.unsubscribe(pairingTopic)
                crypto.removeKeys(pairingTopic.value)
                sequenceStorageRepository.deletePairing(pairingTopic)
            }

        listOfValidPairing
            .filter { pairing -> pairing.status == SequenceStatus.ACKNOWLEDGED }
            .map { pairing -> pairing.topic }
            .onEach { pairingTopic -> relayer.subscribe(pairingTopic) }
    }

    private fun resubscribeToSettledSession() {
        val (listOfExpiredSession, listOfValidSessions) = sequenceStorageRepository.getListOfSessionVOs()
            .partition { session -> !session.expiry.isSequenceValid() }

        listOfExpiredSession
            .map { session -> session.topic }
            .onEach { sessionTopic ->
                relayer.unsubscribe(sessionTopic)
                crypto.removeKeys(sessionTopic.value)
                sequenceStorageRepository.deleteSession(sessionTopic)
            }

        listOfValidSessions
            .filter { session -> session.status == SequenceStatus.ACKNOWLEDGED }
            .onEach { session -> relayer.subscribe(session.topic) }
    }

    private fun isSequenceValid(topic: TopicVO): Boolean =
        sequenceStorageRepository.isSessionValid(topic) || sequenceStorageRepository.isPairingValid(topic)

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
        const val prompt: Boolean = true
    }
}