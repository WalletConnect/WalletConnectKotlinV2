package com.walletconnect.walletconnectv2.engine.domain

import com.walletconnect.walletconnectv2.core.exceptions.client.*
import com.walletconnect.walletconnectv2.core.exceptions.peer.Error
import com.walletconnect.walletconnectv2.core.exceptions.peer.PeerError
import com.walletconnect.walletconnectv2.core.model.type.SequenceLifecycle
import com.walletconnect.walletconnectv2.core.model.type.enums.ControllerType
import com.walletconnect.walletconnectv2.core.model.type.enums.Sequences
import com.walletconnect.walletconnectv2.core.model.utils.JsonRpcMethod
import com.walletconnect.walletconnectv2.core.model.vo.PublicKey
import com.walletconnect.walletconnectv2.core.model.vo.SharedKey
import com.walletconnect.walletconnectv2.core.model.vo.TopicVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.pairing.PairingParamsVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.pairing.after.PostSettlementPairingVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.pairing.after.payload.ProposalRequestVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.pairing.before.success.PairingStateVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.SessionParamsVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.after.PostSettlementSessionVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.after.params.ReasonVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.after.params.SessionRequestVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.before.PreSettlementSessionVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.before.proposal.RelayProtocolOptionsVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.before.proposal.SessionProposerVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.before.success.SessionParticipantVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.common.SessionStateVO
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
    private val metaData: EngineDO.AppMetaData,
    private var controllerType: ControllerType
) {
    private lateinit var sessionPermissions: EngineDO.SessionPermissions

    private val _sequenceEvent: MutableStateFlow<SequenceLifecycle> = MutableStateFlow(EngineDO.Default)
    val sequenceEvent: StateFlow<SequenceLifecycle> = _sequenceEvent

    init {
        resubscribeToSettledSequences()
        setupSequenceExpiration()
        collectJsonRpcRequests()
        collectJsonRpcResponses()
    }

    fun handleInitializationErrors(onError: (WalletConnectException) -> Unit) {
        relayer.initializationErrorsFlow.onEach { walletConnectException -> onError(walletConnectException) }.launchIn(scope)
    }

    internal fun proposeSequence(permissions: EngineDO.SessionPermissions, pairingTopic: String?, onFailure: (Throwable) -> Unit): String? {
        checkPeer(ControllerType.NON_CONTROLLER) {
            throw WalletConnectException.UnauthorizedPeerException(UNAUTHORIZED_CONNECT_MESSAGE)
        }

        Validator.validateSessionPermissions(permissions) { errorMessage ->
            throw WalletConnectException.InvalidSessionPermissionsException(errorMessage)
        }

        if (pairingTopic != null) {
            if (!isSequenceValid(TopicVO(pairingTopic))) {
                throw WalletConnectException.CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE$pairingTopic")
            }
            proposeSession(permissions, pairingTopic) { error -> onFailure(error) }
            return null
        }
        return proposePairing(permissions)
    }

    private fun proposeSession(permissions: EngineDO.SessionPermissions, pairingTopic: String, onFailure: (Throwable) -> Unit = {}) {
        val settledPairing: PairingVO = sequenceStorageRepository.getPairingByTopic(TopicVO(pairingTopic))
        if (settledPairing.status == SequenceStatus.ACKNOWLEDGED) {
            val pendingSessionTopic: TopicVO = generateTopic()
            val selfPublicKey: PublicKey = crypto.generateKeyPair()
            val isController = controllerType == ControllerType.CONTROLLER

            val proposalParams = SessionProposerVO(selfPublicKey.keyAsHex, isController, metaData.toMetaDataVO())
                .toProposalParams(pendingSessionTopic, settledPairing.topic, permissions)

            val proposedSession: SessionVO = proposalParams.toProposedSessionVO(pendingSessionTopic, selfPublicKey, controllerType)
            sequenceStorageRepository.insertPendingSession(proposedSession, metaData.toMetaDataVO(), controllerType)
            relayer.subscribe(pendingSessionTopic)

            val (sharedKey, publicKey) = crypto.getKeyAgreement(settledPairing.topic)
            crypto.setEncryptionKeys(sharedKey as SharedKey, publicKey as PublicKey, pendingSessionTopic)

            val params = PairingParamsVO.PayloadParams(ProposalRequestVO(JsonRpcMethod.WC_SESSION_PROPOSE, params = proposalParams))
            val sessionProposal = PostSettlementPairingVO.PairingPayload(id = generateId(), params = params)

            relayer.publishJsonRpcRequests(settledPairing.topic, sessionProposal, prompt,
                onSuccess = { Logger.log("Session proposal sent successfully") },
                onFailure = { error ->
                    Logger.error("Failed to send a session proposal: $error")
                    onFailure(error)
                }
            )
        }
    }

    private fun proposePairing(permissions: EngineDO.SessionPermissions): String {
        val pendingTopic: TopicVO = generateTopic()
        val publicKey: PublicKey = crypto.generateKeyPair()
        val isController = controllerType == ControllerType.CONTROLLER
        val uri = EngineDO.WalletConnectUri(pendingTopic, publicKey, isController, RelayProtocolOptionsVO())
        val pendingPairing = uri.toProposedPairingVO(controllerType)
        sequenceStorageRepository.insertPendingPairing(pendingPairing, controllerType)
        relayer.subscribe(pendingTopic)
        sessionPermissions = permissions
        return uri.toAbsoluteString()
    }

    internal fun pair(uri: String, onFailure: (Throwable) -> Unit) {
        checkPeer(ControllerType.CONTROLLER) {
            throw WalletConnectException.UnauthorizedPeerException(UNAUTHORIZED_PAIR_MESSAGE)
        }

        val walletConnectUri: EngineDO.WalletConnectUri =
            Validator.validateWCUri(uri) ?: throw WalletConnectException.MalformedWalletConnectUri(MALFORMED_PAIRING_URI_MESSAGE)

        if (sequenceStorageRepository.isPairingValid(walletConnectUri.topic)) {
            throw WalletConnectException.PairWithExistingPairingIsNotAllowed(PAIRING_NOW_ALLOWED_MESSAGE)
        }

        val proposal: PairingParamsVO.Proposal = walletConnectUri.toPairProposal()
        val selfPublicKey: PublicKey = crypto.generateKeyPair()
        val (_, settledTopic) = crypto.generateTopicAndSharedKey(selfPublicKey, PublicKey(proposal.proposer.publicKey))


        val respondedPairing = proposal.toRespondedPairingVO(settledTopic, selfPublicKey, uri, controllerType)
        val preSettledPairing = proposal.toPreSettledPairingVO(settledTopic, selfPublicKey, uri, controllerType)

        relayer.subscribe(proposal.topic)
        sequenceStorageRepository.insertPendingPairing(respondedPairing, controllerType, settledTopic)

        relayer.subscribe(settledTopic)
        sequenceStorageRepository.insertSettledPairing(preSettledPairing, controllerType)

        val preSettlementPairingApprove = proposal.toApprove(generateId(), preSettledPairing.expiry, selfPublicKey)

        relayer.isConnectionOpened
            .filter { isOnline -> isOnline }
            .onEach {
                relayer.publishJsonRpcRequests(proposal.topic, preSettlementPairingApprove,
                    onSuccess = { Logger.log("Pairing approval sent successfully") },
                    onFailure = { throwable ->
                        Logger.error("Sending pairing approve error: $throwable")
                        onFailure(throwable)
                    }
                )

            }.launchIn(scope)
    }

    internal fun approve(proposal: EngineDO.SessionProposal, onFailure: (Throwable) -> Unit) {
        checkPeer(ControllerType.CONTROLLER) {
            throw WalletConnectException.UnauthorizedPeerException(UNAUTHORIZED_APPROVE_MESSAGE)
        }

        Validator.validateProposalFields(proposal) { errorMessage ->
            throw WalletConnectException.InvalidSessionProposalException(errorMessage)
        }

        Validator.validateCAIP10(proposal.accounts) { errorMessage ->
            throw WalletConnectException.InvalidAccountsException(errorMessage)
        }

        Validator.validateIfChainIdsIncludedInPermission(proposal.accounts, proposal.chains) { errorMessage ->
            throw WalletConnectException.InvalidAccountsException(errorMessage)
        }

        Validator.validateSessionPermissions(proposal.toSessionPermissions()) { errorMessage ->
            throw WalletConnectException.InvalidSessionPermissionsException(errorMessage)
        }

        val selfPublicKey: PublicKey = crypto.generateKeyPair()
        val (_, settledTopic) = crypto.generateTopicAndSharedKey(selfPublicKey, PublicKey(proposal.publicKey))

        val pendingSession = proposal.toRespondedSessionVO(selfPublicKey, settledTopic, controllerType)
        val settledSession = proposal.toPreSettledSessionVO(settledTopic, selfPublicKey, controllerType)

        sequenceStorageRepository.insertPendingSession(pendingSession, metaData.toMetaDataVO(), controllerType)
        relayer.subscribe(TopicVO(proposal.topic))

        sequenceStorageRepository.insertSettledSession(settledSession, metaData.toMetaDataVO(), controllerType)
        relayer.subscribe(settledTopic)

        val params = SessionParamsVO.ApprovalParams(
            relay = RelayProtocolOptionsVO(),
            state = SessionStateVO(proposal.accounts),
            expiry = settledSession.expiry,
            responder = SessionParticipantVO(selfPublicKey.keyAsHex, metadata = metaData.toMetaDataVO())
        )

        val sessionApprove = PreSettlementSessionVO.Approve(id = generateId(), params = params)
        relayer.publishJsonRpcRequests(TopicVO(proposal.topic), sessionApprove,
            onSuccess = { Logger.log("Session approval sent successfully") },
            onFailure = { error ->
                Logger.error("Sending session approve error: $error")
                onFailure(error)
            }
        )
    }

    internal fun reject(reason: String, topic: String, onFailure: (Throwable) -> Unit) {
        checkPeer(ControllerType.CONTROLLER) {
            throw WalletConnectException.UnauthorizedPeerException(UNAUTHORIZED_REJECT_MESSAGE)
        }

        val params = SessionParamsVO.RejectParams(reason = ReasonVO(message = reason))
        val sessionReject = PreSettlementSessionVO.Reject(id = generateId(), params = params)
        relayer.publishJsonRpcRequests(TopicVO(topic), sessionReject,
            onSuccess = { Logger.log("Session reject sent successfully") },
            onFailure = { error ->
                Logger.error("Sending session reject error: $error")
                onFailure(error)
            }
        )
    }

    internal fun upgrade(topic: String, permissions: EngineDO.SessionPermissions, onFailure: (Throwable) -> Unit) {
        checkPeer(ControllerType.CONTROLLER) {
            throw WalletConnectException.UnauthorizedPeerException(UNAUTHORIZED_UPGRADE_MESSAGE)
        }

        if (!isSequenceValid(TopicVO(topic))) {
            throw WalletConnectException.CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE$topic")
        }

        Validator.validateSessionPermissions(permissions) { errorMessage ->
            throw WalletConnectException.InvalidSessionPermissionsException(errorMessage)
        }

        val upgradePermissionsParams = SessionParamsVO.UpgradeParams(permissions = permissions.toSessionsPermissions())
        val sessionUpgrade = PostSettlementSessionVO.SessionUpgrade(id = generateId(), params = upgradePermissionsParams)
        sequenceStorageRepository.upgradeSessionWithPermissions(TopicVO(topic), permissions.blockchain.chains, permissions.jsonRpc.methods)

        relayer.publishJsonRpcRequests(TopicVO(topic), sessionUpgrade,
            onSuccess = { Logger.log("Session upgrade sent successfully") },
            onFailure = { error ->
                Logger.error("Sending session upgrade error: $error")
                onFailure(error)
            }
        )
    }

    internal fun update(topic: String, state: EngineDO.SessionState, onFailure: (Throwable) -> Unit) {
        checkPeer(ControllerType.CONTROLLER) {
            throw WalletConnectException.UnauthorizedPeerException(UNAUTHORIZED_UPDATE_MESSAGE)
        }

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


        val params = SessionParamsVO.UpdateParams(SessionStateVO(state.accounts))
        val sessionUpdate: PostSettlementSessionVO.SessionUpdate = PostSettlementSessionVO.SessionUpdate(id = generateId(), params = params)
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
        if (!isSequenceValid(TopicVO(request.topic))) {
            throw WalletConnectException.CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE${request.topic}")
        }

        val chains: List<String> = sequenceStorageRepository.getSessionByTopic(TopicVO(request.topic)).chains
        Validator.validateChainIdAuthorization(request.chainId, chains) { errorMessage ->
            throw WalletConnectException.UnauthorizedChainIdException(errorMessage)
        }

        val params =
            SessionParamsVO.SessionPayloadParams(request = SessionRequestVO(request.method, request.params), chainId = request.chainId)
        val sessionPayload = PostSettlementSessionVO.SessionPayload(id = generateId(), params = params)
        relayer.publishJsonRpcRequests(TopicVO(request.topic), sessionPayload, prompt,
            onSuccess = {
                Logger.log("Session request sent successfully")
                scope.launch {
                    try {
                        withTimeout(FIVE_MINUTES_TIMEOUT) {
                            collectResponse(sessionPayload.id) { result ->
                                cancel()
                                result.fold(
                                    onSuccess = {},
                                    onFailure = { error -> onFailure(Throwable(error)) }
                                )
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

        relayer.publishJsonRpcResponse(
            TopicVO(topic), jsonRpcResponse,
            { Logger.log("Session payload sent successfully") },
            { error ->
                onFailure(error)
                Logger.error("Sending session payload response error: $error")
            })
    }

    internal fun ping(topic: String, onSuccess: (String) -> Unit, onFailure: (Throwable) -> Unit) {
        val pingParams = when {
            sequenceStorageRepository.isSessionValid(TopicVO(topic)) ->
                PostSettlementSessionVO.SessionPing(id = generateId(), params = SessionParamsVO.PingParams())
            sequenceStorageRepository.isPairingValid(TopicVO(topic)) ->
                PostSettlementPairingVO.PairingPing(id = generateId(), params = PairingParamsVO.PingParams())
            else -> throw WalletConnectException.CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE$topic")
        }

        relayer.publishJsonRpcRequests(TopicVO(topic), pingParams,
            onSuccess = {
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

        val notificationParams = SessionParamsVO.NotificationParams(notification.type, notification.data)
        val sessionNotification = PostSettlementSessionVO.SessionNotification(id = generateId(), params = notificationParams)

        relayer.publishJsonRpcRequests(TopicVO(topic), sessionNotification,
            onSuccess = { Logger.error("Notify sent successfully") },
            onFailure = { error ->
                Logger.error("Sending notify error: $error")
                onFailure(error)
            }
        )
    }

    internal fun disconnect(topic: String, reason: String, code: Int) {
        if (!isSequenceValid(TopicVO(topic))) {
            throw WalletConnectException.CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE$topic")
        }

        val deleteParams = SessionParamsVO.DeleteParams(ReasonVO(message = reason, code = code))
        val sessionDelete = PostSettlementSessionVO.SessionDelete(id = generateId(), params = deleteParams)
        sequenceStorageRepository.deleteSession(TopicVO(topic))
        relayer.unsubscribe(TopicVO(topic))
        relayer.publishJsonRpcRequests(TopicVO(topic), sessionDelete,
            onSuccess = { Logger.error("Disconnect sent successfully") },
            onFailure = { error -> Logger.error("Sending session disconnect error: $error") })
    }

    private suspend fun collectResponse(id: Long, onResponse: (Result<JsonRpcResponseVO.JsonRpcResult>) -> Unit) {
        relayer.peerResponse
            .filter { response -> response.result.id == id }
            .collect { response ->
                when (val result = response.result) {
                    is JsonRpcResponseVO.JsonRpcResult -> onResponse(Result.success(result))
                    is JsonRpcResponseVO.JsonRpcError -> onResponse(Result.failure(Throwable(result.errorMessage)))
                }
            }
    }

    internal fun getListOfSettledSessions(): List<EngineDO.SettledSession> {
        return sequenceStorageRepository.getListOfSessionVOs()
            .filter { session -> session.status == SequenceStatus.ACKNOWLEDGED && session.expiry.isSequenceValid() }
            .map { session -> session.toEngineDOSettledSessionVO() }
    }

    internal fun getListOfSettledPairings(): List<EngineDO.SettledPairing> {
        return sequenceStorageRepository.getListOfPairingVOs()
            .filter { pairing -> pairing.status == SequenceStatus.ACKNOWLEDGED && pairing.expiry.isSequenceValid() }
            .map { pairing -> pairing.toEngineDOSettledPairing() }
    }

    internal fun getPendingRequests(topic: TopicVO): List<PendingRequestVO> = relayer.getPendingRequests(topic)

    private fun collectJsonRpcRequests() {
        scope.launch {
            relayer.clientSyncJsonRpc.collect { request ->
                when (val requestParams = request.params) {
                    is PairingParamsVO.PayloadParams -> onPairingPayload(request, requestParams)
                    is PairingParamsVO.ApproveParams -> onPairingApprove(request, requestParams)
                    is PairingParamsVO.DeleteParams -> onPairingDelete(request, requestParams)
                    is PairingParamsVO.UpdateParams -> onPairingUpdate(request, requestParams)
                    is SessionParamsVO.ApprovalParams -> onSessionApprove(request, requestParams)
                    is SessionParamsVO.RejectParams -> onSessionReject(request, requestParams)
                    is SessionParamsVO.DeleteParams -> onSessionDelete(request, requestParams)
                    is SessionParamsVO.SessionPayloadParams -> onSessionPayload(request, requestParams)
                    is SessionParamsVO.UpdateParams -> onSessionUpdate(request, requestParams)
                    is SessionParamsVO.UpgradeParams -> onSessionUpgrade(request, requestParams)
                    is SessionParamsVO.NotificationParams -> onSessionNotification(request, requestParams)
                    is SessionParamsVO.PingParams, is PairingParamsVO.PingParams -> onPing(request)
                }
            }
        }
    }

    private fun onPairingPayload(request: WCRequestVO, payloadParams: PairingParamsVO.PayloadParams) {
        if (!isSequenceValid(request.topic)) {
            relayer.respondWithError(request, PeerError(Error.NoMatchingTopic(Sequences.PAIRING.name, request.topic.value)))
            return
        }

        if (payloadParams.request.method != JsonRpcMethod.WC_SESSION_PROPOSE) {
            relayer.respondWithError(request, PeerError(Error.UnauthorizedJsonRpcMethod(request.method)))
            return
        }

        val proposal: SessionParamsVO.ProposalParams = payloadParams.request.params
        val isController: Boolean = controllerType == ControllerType.CONTROLLER
        if (proposal.proposer.controller == isController) {
            relayer.respondWithError(request, PeerError(Error.UnauthorizedMatchingController(isController)))
            return
        }

        val (sharedKey, publicKey) = crypto.getKeyAgreement(proposal.signal.params.topic)
        crypto.setEncryptionKeys(sharedKey as SharedKey, publicKey as PublicKey, proposal.topic)

        relayer.respondWithSuccess(request)
        _sequenceEvent.value = payloadParams.toEngineDOSessionProposal()
    }

    private fun onPairingApprove(request: WCRequestVO, params: PairingParamsVO.ApproveParams) {
        if (!isSequenceValid(request.topic)) {
            relayer.respondWithError(request, PeerError(Error.NoMatchingTopic(Sequences.PAIRING.name, request.topic.value)))
            return
        }

        val pendingPairing: PairingVO = sequenceStorageRepository.getPairingByTopic(request.topic)
        if (pendingPairing.status != SequenceStatus.PROPOSED) {
            relayer.respondWithError(request, PeerError(Error.NoMatchingTopic(Sequences.PAIRING.name, request.topic.value)))
            return
        }

        val (_, settledTopic) = crypto.generateTopicAndSharedKey(pendingPairing.selfParticipant, PublicKey(params.responder.publicKey))
        val settledPairing = pendingPairing.toAcknowledgedPairingVO(settledTopic, params, controllerType)

        sequenceStorageRepository.insertSettledPairing(settledPairing, controllerType)
        sequenceStorageRepository.deletePairing(pendingPairing.topic)

        relayer.subscribe(settledTopic)
        relayer.unsubscribe(pendingPairing.topic)

        if (!this::sessionPermissions.isInitialized) {
            Logger.error("Cannot find permissions for pending session")
            return
        }

        relayer.respondWithSuccess(request)
        proposeSession(sessionPermissions, settledTopic.value)
        _sequenceEvent.value = settledPairing.toEngineDOSettledPairing()
    }

    private fun onPairingDelete(request: WCRequestVO, params: PairingParamsVO.DeleteParams) {
        if (!isSequenceValid(request.topic)) {
            relayer.respondWithError(request, PeerError(Error.NoMatchingTopic(Sequences.PAIRING.name, request.topic.value)))
            return
        }

        crypto.removeKeys(request.topic.value)
        relayer.unsubscribe(request.topic)
        sequenceStorageRepository.deletePairing(request.topic)
        _sequenceEvent.value = EngineDO.DeletedPairing(request.topic.value, params.reason.message)
    }

    private fun onPairingUpdate(request: WCRequestVO, params: PairingParamsVO.UpdateParams) {
        if (!isSequenceValid(request.topic)) {
            relayer.respondWithError(request, PeerError(Error.NoMatchingTopic(Sequences.PAIRING.name, request.topic.value)))
            return
        }

        if (params.state.metadata == null) {
            relayer.respondWithError(request, PeerError(Error.InvalidUpdateRequest(Sequences.PAIRING.name)))
            return
        }

        val pairing: PairingVO = sequenceStorageRepository.getPairingByTopic(request.topic)
        if (!pairing.isPeerController) {
            relayer.respondWithError(request, PeerError(Error.UnauthorizedUpdateRequest(Sequences.PAIRING.name)))
            return
        }

        sequenceStorageRepository.updateAcknowledgedPairingMetadata(params.state.metadata, pairing.topic)
        relayer.respondWithSuccess(request)
        _sequenceEvent.value = EngineDO.PairingUpdate(request.topic, params.state.metadata.toEngineDOMetaData())
    }

    private fun onSessionApprove(request: WCRequestVO, params: SessionParamsVO.ApprovalParams) {
        if (!isSequenceValid(request.topic)) {
            relayer.respondWithError(request, PeerError(Error.NoMatchingTopic(Sequences.SESSION.name, request.topic.value)))
            return
        }

        if (controllerType != ControllerType.NON_CONTROLLER) {
            relayer.respondWithError(request, PeerError(Error.UnauthorizedMatchingController(true)))
            return
        }

        val pendingSession: SessionVO = sequenceStorageRepository.getSessionByTopic(request.topic)
        if (pendingSession.status != SequenceStatus.PROPOSED) {
            relayer.respondWithError(request, PeerError(Error.NoMatchingTopic(Sequences.SESSION.name, request.topic.value)))
            return
        }

        val (_, settledTopic) = crypto.generateTopicAndSharedKey(pendingSession.selfParticipant, PublicKey(params.responder.publicKey))
        val settledSession = pendingSession.toEngineDOAcknowledgeSessionVO(settledTopic, params)

        sequenceStorageRepository.deleteSession(pendingSession.topic)
        sequenceStorageRepository.insertSettledSession(settledSession, metaData.toMetaDataVO(), controllerType)

        relayer.subscribe(settledTopic)
        relayer.unsubscribe(pendingSession.topic)

        relayer.respondWithSuccess(request)
        _sequenceEvent.value = pendingSession.toSessionApproved(params, settledTopic)
    }

    private fun onSessionReject(request: WCRequestVO, params: SessionParamsVO.RejectParams) {
        if (!isSequenceValid(request.topic)) {
            relayer.respondWithError(request, PeerError(Error.NoMatchingTopic(Sequences.SESSION.name, request.topic.value)))
            return
        }

        sequenceStorageRepository.deleteSession(request.topic)
        relayer.unsubscribe(request.topic)
        relayer.respondWithSuccess(request)
        _sequenceEvent.value = EngineDO.SessionRejected(request.topic.value, params.reason.message)
    }

    private fun onSessionDelete(request: WCRequestVO, params: SessionParamsVO.DeleteParams) {
        if (!isSequenceValid(request.topic)) {
            relayer.respondWithError(request, PeerError(Error.NoMatchingTopic(Sequences.SESSION.name, request.topic.value)))
            return
        }

        crypto.removeKeys(request.topic.value)
        sequenceStorageRepository.deleteSession(request.topic)
        relayer.unsubscribe(request.topic)
        relayer.respondWithSuccess(request)
        _sequenceEvent.value = params.toEngineDoDeleteSession(request.topic)
    }

    private fun onSessionPayload(request: WCRequestVO, params: SessionParamsVO.SessionPayloadParams) {
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

        _sequenceEvent.value = params.toEngineDOSessionRequest(request)
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

        Validator.validateCAIP10(params.state.accounts) {
            relayer.respondWithError(request, PeerError(Error.InvalidUpdateRequest(Sequences.SESSION.name)))
            return@validateCAIP10
        }

        sequenceStorageRepository.updateSessionWithAccounts(session.topic, params.state.accounts)
        relayer.respondWithSuccess(request)
        _sequenceEvent.value = EngineDO.SessionUpdate(request.topic, params.state.accounts)
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

        val chains = params.permissions.blockchain.chains
        val methods = params.permissions.jsonRpc.methods

        Validator.validateSessionPermissions(params.permissions.toEngineDOPermissions()) {
            relayer.respondWithError(request, PeerError(Error.InvalidUpgradeRequest(Sequences.SESSION.name)))
            return@validateSessionPermissions
        }

        sequenceStorageRepository.upgradeSessionWithPermissions(request.topic, chains, methods)
        relayer.respondWithSuccess(request)

        val chainsUnion = session.chains.union(chains).toList()
        val methodsUnion = session.methods.union(methods).toList()
        _sequenceEvent.value = EngineDO.SessionUpgrade(session.topic, chainsUnion, methodsUnion)
    }

    private fun onSessionNotification(request: WCRequestVO, params: SessionParamsVO.NotificationParams) {
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
        _sequenceEvent.value = params.toEngineDoSessionNotification(request.topic)
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
                    is PairingParamsVO.ApproveParams -> onPairingApprovalResponse(response)
                    is PairingParamsVO.UpdateParams -> onPairingUpdateResponse(response)
                    is PairingParamsVO.PayloadParams -> onSessionProposalResponse(response, params)
                    is SessionParamsVO.ApprovalParams -> onSessionApproveResponse(response)
                    is SessionParamsVO.UpgradeParams -> onSessionUpgradeResponse(response)
                    is SessionParamsVO.UpdateParams -> onSessionUpdateResponse(response)
                    is SessionParamsVO.SessionPayloadParams -> onSessionRequestResponse(response, params)
                }
            }
        }
    }

    private fun onPairingApprovalResponse(response: WCResponseVO) {
        val pendingTopic = response.topic
        if (!isSequenceValid(pendingTopic)) return //topic A
        val pendingPairing = sequenceStorageRepository.getPairingByTopic(pendingTopic)
        val settledTopic = pendingPairing.outcomeTopic //topic B
        if (settledTopic.value.isEmpty() || !isSequenceValid(settledTopic)) return

        when (val result = response.result) {
            is JsonRpcResponseVO.JsonRpcResult -> {
                Logger.log("Pairing approval response received")
                val settledPairing = sequenceStorageRepository.getPairingByTopic(settledTopic)
                sequenceStorageRepository.updatePreSettledPairingToAcknowledged(settledPairing.copy(status = SequenceStatus.ACKNOWLEDGED))
                relayer.unsubscribe(pendingTopic)
                sequenceStorageRepository.deletePairing(pendingTopic)
                _sequenceEvent.value = EngineDO.SettledPairingResponse.Result(settledPairing.topic)
                pairingUpdate(settledPairing)
            }
            is JsonRpcResponseVO.JsonRpcError -> {
                Logger.log("Pairing approval error response received: ${result.error}")
                _sequenceEvent.value = EngineDO.SettledPairingResponse.Error(result.errorMessage)
            }
        }
    }

    private fun pairingUpdate(settledSequence: PairingVO) {
        val params = PairingParamsVO.UpdateParams(state = PairingStateVO(metaData.toMetaDataVO()))
        val pairingUpdate: PostSettlementPairingVO.PairingUpdate = PostSettlementPairingVO.PairingUpdate(id = generateId(), params = params)

        relayer.publishJsonRpcRequests(settledSequence.topic, pairingUpdate,
            onSuccess = { Logger.error("Pairing update sent successfully") },
            onFailure = { error -> Logger.error("Sending pairing update error: $error") }
        )
    }

    private fun onPairingUpdateResponse(response: WCResponseVO) {
        when (val result = response.result) {
            is JsonRpcResponseVO.JsonRpcResult -> {
                Logger.log("Pairing update response received")
                sequenceStorageRepository.updateAcknowledgedPairingMetadata(metaData.toMetaDataVO(), response.topic)
            }
            is JsonRpcResponseVO.JsonRpcError -> Logger.log("Pairing update error response received: ${result.error}")
        }
    }

    private fun onSessionProposalResponse(response: WCResponseVO, params: PairingParamsVO.PayloadParams) {
        when (val result = response.result) {
            is JsonRpcResponseVO.JsonRpcResult -> Logger.log("Session proposal response received")
            is JsonRpcResponseVO.JsonRpcError -> {
                Logger.log("Session proposal error response received: ${result.error}")
                with(params.request.params) {
                    relayer.unsubscribe(response.topic)
                    sequenceStorageRepository.deleteSession(topic)
                    crypto.removeKeys(topic.value)
                }
            }
        }
    }

    private fun onSessionApproveResponse(response: WCResponseVO) {
        val pendingTopic = response.topic
        if (!isSequenceValid(pendingTopic)) return //topic C
        val pendingSession: SessionVO = sequenceStorageRepository.getSessionByTopic(pendingTopic)
        val settledTopic = pendingSession.outcomeTopic //topic D
        if (settledTopic.value.isEmpty()) return

        when (val result = response.result) {
            is JsonRpcResponseVO.JsonRpcResult -> {
                Logger.log("Session approve response received")
                if (!isSequenceValid(settledTopic)) return
                val settledSession = sequenceStorageRepository.getSessionByTopic(settledTopic)
                relayer.unsubscribe(pendingTopic)
                sequenceStorageRepository.deleteSession(pendingTopic)
                crypto.removeKeys(pendingTopic.value)
                sequenceStorageRepository.updatePreSettledSessionToAcknowledged(settledSession.copy(status = SequenceStatus.ACKNOWLEDGED))
                Logger.error("kobe; settled session: $settledSession")
                _sequenceEvent.value =
                    EngineDO.SettledSessionResponse.Result(settledSession.toEngineDOSettledSessionVO(settledTopic, settledSession.expiry))
            }
            is JsonRpcResponseVO.JsonRpcError -> {
                Logger.error("Session approval error response received: ${result.error}")
                relayer.unsubscribe(pendingTopic)
                relayer.unsubscribe(settledTopic)
                crypto.removeKeys(pendingSession.topic.value)
                crypto.removeKeys(settledTopic.value)
                sequenceStorageRepository.deleteSession(settledTopic)
                sequenceStorageRepository.deleteSession(pendingTopic)
                _sequenceEvent.value = EngineDO.SettledSessionResponse.Error(result.errorMessage)
            }
        }
    }

    private fun onSessionUpgradeResponse(response: WCResponseVO) {
        if (!isSequenceValid(response.topic)) return
        val session = sequenceStorageRepository.getSessionByTopic(response.topic)

        when (val result = response.result) {
            is JsonRpcResponseVO.JsonRpcResult -> {
                Logger.log("Session upgrade response received")
                _sequenceEvent.value = EngineDO.SessionUpgradeResponse.Result(session.topic, session.chains, session.methods)
            }
            is JsonRpcResponseVO.JsonRpcError -> {
                Logger.error("Peer failed to upgrade session: ${result.error}")
                _sequenceEvent.value = EngineDO.SessionUpgradeResponse.Error(result.errorMessage)
            }
        }
    }

    private fun onSessionUpdateResponse(response: WCResponseVO) {
        if (!isSequenceValid(response.topic)) return
        val session = sequenceStorageRepository.getSessionByTopic(response.topic)

        when (val result = response.result) {
            is JsonRpcResponseVO.JsonRpcResult -> {
                Logger.log("Session update response received")
                _sequenceEvent.value = EngineDO.SessionUpdateResponse.Result(session.topic, session.accounts)
            }
            is JsonRpcResponseVO.JsonRpcError -> {
                Logger.error("Peer failed to update session: ${result.error}")
                _sequenceEvent.value = EngineDO.SessionUpdateResponse.Error(result.errorMessage)
            }
        }
    }

    private fun onSessionRequestResponse(response: WCResponseVO, params: SessionParamsVO.SessionPayloadParams) {
        val result = when (response.result) {
            is JsonRpcResponseVO.JsonRpcResult -> response.result.toEngineJsonRpcResult()
            is JsonRpcResponseVO.JsonRpcError -> response.result.toEngineJsonRpcError()
        }
        _sequenceEvent.value = EngineDO.SessionPayloadResponse(response.topic.value, params.chainId, result)
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

    private fun checkPeer(requiredPeer: ControllerType, onUnauthorizedPeer: () -> Unit) {
        if (controllerType != requiredPeer) {
            onUnauthorizedPeer()
        }
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