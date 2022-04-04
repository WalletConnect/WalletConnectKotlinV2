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
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.common.SessionParticipantVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.pairing.PairingSettlementVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.pairing.params.PairingParamsVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.SessionSettlementVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.params.SessionParamsVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.payload.BlockchainSettledVO
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
        Validator.validatePermissions(permissions.jsonRpc, permissions.notifications) { errorMessage ->
            throw WalletConnectException.InvalidSessionPermissionsException(errorMessage)
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
        relayer.respondWithError(request, PeerError(Error.UserError(reason, code)), onFailure = { error -> onFailure(error) })
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
        Validator.validatePermissions(permissions.jsonRpc, permissions.notifications) { errorMessage ->
            throw WalletConnectException.InvalidSessionPermissionsException(errorMessage)
        }

        val selfPublicKey: PublicKey = crypto.generateKeyPair()
        val (_, sessionTopic) = crypto.generateTopicAndSharedKey(selfPublicKey, PublicKey(proposal.proposerPublicKey))
        relayer.subscribe(sessionTopic)
        val approvalParams = proposal.toSessionApproveParams(selfPublicKey, metaData.toMetaDataVO())
        relayer.respondWithParams(request, approvalParams)
        sessionSettle(proposal, sessionTopic) { error -> onFailure(error) }
    }

    internal fun upgrade(topic: String, permissions: EngineDO.SessionPermissions, onFailure: (Throwable) -> Unit) {
        if (!sequenceStorageRepository.isSessionValid(TopicVO(topic))) {
            throw WalletConnectException.CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE$topic")
        }

        Validator.validatePermissions(permissions.jsonRpc, permissions.notifications) { errorMessage ->
            throw WalletConnectException.InvalidSessionPermissionsException(errorMessage)
        }

        val session = sequenceStorageRepository.getSessionByTopic(TopicVO(topic))
        if (!session.isSelfController) {
            throw WalletConnectException.UnauthorizedPeerException(UNAUTHORIZED_UPGRADE_MESSAGE)
        }

        if (!session.isAcknowledged) {
            throw WalletConnectException.NotSettledSessionException("$SESSION_IS_NOT_ACKNOWLEDGED_MESSAGE$topic")
        }

        val upgradePermissionsParams = SessionParamsVO.UpgradeParams(permissions = permissions.toSessionsPermissionsVO())
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

        val params = SessionParamsVO.UpdateParams(BlockchainSettledVO(state.accounts, Validator.getChainIds(state.accounts)))
        val sessionUpdate: SessionSettlementVO.SessionUpdate = SessionSettlementVO.SessionUpdate(id = generateId(), params = params)
        sequenceStorageRepository.updateSessionWithAccounts(TopicVO(topic), state.accounts)
        relayer.publishJsonRpcRequests(TopicVO(topic), sessionUpdate,
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

    internal fun notify(topic: String, notification: EngineDO.Notification, onFailure: (Throwable) -> Unit) {
        if (!sequenceStorageRepository.isSessionValid(TopicVO(topic))) {
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

    fun sessionExtend(topic: String, newExpiration: Long, onFailure: (Throwable) -> Unit) {
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
            SessionSettlementVO.SessionExtend(id = generateId(), params = SessionParamsVO.ExtendParams(expiry = newExpiration))
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
                    is SessionParamsVO.UpdateParams -> onSessionUpdate(request, requestParams)
                    is SessionParamsVO.UpgradeParams -> onSessionUpgrade(request, requestParams)
                    is SessionParamsVO.NotifyParams -> onSessionNotification(request, requestParams)
                    is SessionParamsVO.ExtendParams -> onSessionExtend(request, requestParams)
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
            relayer.respondWithError(request, PeerError(Error.NoMatchingTopic(Sequences.PAIRING.name, request.topic.value)))
            return
        }

        crypto.removeKeys(request.topic.value)
        relayer.unsubscribe(request.topic)
        sequenceStorageRepository.deletePairing(request.topic)
        scope.launch { _sequenceEvent.emit(EngineDO.DeletedPairing(request.topic.value, params.reason.message)) }
    }

    private fun onSessionDelete(request: WCRequestVO, params: SessionParamsVO.DeleteParams) {
        if (!sequenceStorageRepository.isSessionValid(request.topic)) {
            relayer.respondWithError(request, PeerError(Error.NoMatchingTopic(Sequences.SESSION.name, request.topic.value)))
            return
        }

        crypto.removeKeys(request.topic.value)
        sequenceStorageRepository.deleteSession(request.topic)
        relayer.unsubscribe(request.topic)
        scope.launch { _sequenceEvent.emit(params.toEngineDoDeleteSession(request.topic)) }
    }

    private fun onSessionRequest(request: WCRequestVO, params: SessionParamsVO.SessionRequestParams) {
        if (!sequenceStorageRepository.isSessionValid(request.topic)) {
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
        if (!sequenceStorageRepository.isSessionValid(request.topic)) {
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
        if (!sequenceStorageRepository.isSessionValid(request.topic)) {
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
        if (!sequenceStorageRepository.isSessionValid(request.topic)) {
            relayer.respondWithError(request, PeerError(Error.NoMatchingTopic(Sequences.SESSION.name, request.topic.value)))
            return
        }

        val session = sequenceStorageRepository.getSessionByTopic(request.topic)
        if (!session.isAcknowledged) {
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

    private fun onSessionExtend(request: WCRequestVO, requestParams: SessionParamsVO.ExtendParams) {
        if (!sequenceStorageRepository.isSessionValid(request.topic)) {
            relayer.respondWithError(request, PeerError(Error.NoMatchingTopic(Sequences.SESSION.name, request.topic.value)))
            return
        }

        val session = sequenceStorageRepository.getSessionByTopic(request.topic)
        if (!session.isSelfController) {
            relayer.respondWithError(request, PeerError(Error.UnauthorizedExtendRequest(Sequences.SESSION.name)))
            return
        }

        val ttl = requestParams.expiry
        Validator.validateSessionExtend(ttl, session.expiry.seconds) {
            relayer.respondWithError(request, PeerError(Error.UnauthorizedExtendRequest(Sequences.SESSION.name)))
            return@validateSessionExtend
        }

        sequenceStorageRepository.updateSessionExpiry(request.topic, ttl)
        relayer.respondWithSuccess(request)
        scope.launch { _sequenceEvent.emit(session.toEngineDOExtendedSessionVO(ExpiryVO(ttl))) }
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
                    is SessionParamsVO.UpgradeParams -> onSessionUpgradeResponse(response)
                    is SessionParamsVO.UpdateParams -> onSessionUpdateResponse(response)
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

    private fun onSessionUpgradeResponse(wcResponse: WCResponseVO) {
        val sessionTopic = wcResponse.topic
        if (!sequenceStorageRepository.isSessionValid(sessionTopic)) return
        val session = sequenceStorageRepository.getSessionByTopic(sessionTopic)

        when (val response = wcResponse.response) {
            is JsonRpcResponseVO.JsonRpcResult -> {
                Logger.log("Session upgrade response received")
                scope.launch { _sequenceEvent.emit(EngineDO.SessionUpgradeResponse.Result(session.topic, session.methods, session.types)) }
            }
            is JsonRpcResponseVO.JsonRpcError -> {
                Logger.error("Peer failed to upgrade session: ${response.error}")
                scope.launch { _sequenceEvent.emit(EngineDO.SessionUpgradeResponse.Error(response.errorMessage)) }
            }
        }
    }

    private fun onSessionUpdateResponse(wcResponse: WCResponseVO) {
        val sessionTopic = wcResponse.topic
        if (!sequenceStorageRepository.isSessionValid(sessionTopic)) return
        val session = sequenceStorageRepository.getSessionByTopic(sessionTopic)

        when (val response = wcResponse.response) {
            is JsonRpcResponseVO.JsonRpcResult -> {
                Logger.log("Session update response received")
                scope.launch { _sequenceEvent.emit(EngineDO.SessionUpdateResponse.Result(session.topic, session.accounts)) }
            }
            is JsonRpcResponseVO.JsonRpcError -> {
                Logger.error("Peer failed to update session: ${response.error}")
                scope.launch { _sequenceEvent.emit(EngineDO.SessionUpdateResponse.Error(response.errorMessage)) }
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