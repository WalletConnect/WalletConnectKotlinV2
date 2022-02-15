package com.walletconnect.walletconnectv2.engine.domain

import com.walletconnect.walletconnectv2.core.exceptions.client.*
import com.walletconnect.walletconnectv2.core.exceptions.peer.Error
import com.walletconnect.walletconnectv2.core.exceptions.peer.PeerError
import com.walletconnect.walletconnectv2.core.exceptions.peer.Sequence
import com.walletconnect.walletconnectv2.core.model.type.ControllerType
import com.walletconnect.walletconnectv2.core.model.type.SequenceLifecycle
import com.walletconnect.walletconnectv2.core.model.utils.JsonRpcMethod
import com.walletconnect.walletconnectv2.core.model.vo.*
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
import com.walletconnect.walletconnectv2.core.model.vo.sequence.PairingVO
import com.walletconnect.walletconnectv2.core.model.vo.sequence.SessionVO
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
        relayer.isConnectionOpened
            .filter { isConnected: Boolean -> isConnected }
            .onEach {
                coroutineScope {
                    launch(Dispatchers.IO) { resubscribeToSettledPairings() }
                    launch(Dispatchers.IO) { resubscribeToSettledSession() }
                }
            }.launchIn(scope)

        collectClientSyncJsonRpc()
        setupSequenceExpiration()
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
            sequenceStorageRepository.insertSessionProposal(proposedSession, metaData.toMetaDataVO(), controllerType)
            relayer.subscribe(pendingSessionTopic)

            val (sharedKey, publicKey) = crypto.getKeyAgreement(settledPairing.topic)
            crypto.setEncryptionKeys(sharedKey as SharedKey, publicKey as PublicKey, pendingSessionTopic)

            val params = PairingParamsVO.PayloadParams(ProposalRequestVO(JsonRpcMethod.WC_SESSION_PROPOSE, params = proposalParams))
            val sessionProposal = PostSettlementPairingVO.PairingPayload(id = generateId(), params = params)

            relayer.publishJsonRpcRequests(settledPairing.topic, sessionProposal) { result ->
                result.fold(
                    onSuccess = { Logger.log("Session proposal response received") },
                    onFailure = { error ->
                        Logger.error("Failed to send a session proposal: $error")
                        with(proposalParams) {
                            relayer.unsubscribe(topic)
                            sequenceStorageRepository.deleteSession(topic)
                            crypto.removeKeys(topic.value)
                        }
                        onFailure(error)
                    }
                )
            }
        }
    }

    private fun proposePairing(permissions: EngineDO.SessionPermissions): String {
        val topic: TopicVO = generateTopic()
        val publicKey: PublicKey = crypto.generateKeyPair()
        val isController = controllerType == ControllerType.CONTROLLER
        val uri = EngineDO.WalletConnectUri(topic, publicKey, isController, RelayProtocolOptionsVO())
        val proposedPairing = uri.toProposedPairingVO(controllerType)
        sequenceStorageRepository.insertPendingPairing(proposedPairing, controllerType)
        relayer.subscribe(topic)
        sessionPermissions = permissions
        return uri.toAbsoluteString()
    }

    internal fun pair(uri: String, onSuccess: (String) -> Unit, onFailure: (Throwable) -> Unit) {
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
        sequenceStorageRepository.insertPendingPairing(respondedPairing, controllerType)

        relayer.subscribe(settledTopic)
        sequenceStorageRepository.updateRespondedPairingToPreSettled(proposal.topic, preSettledPairing)

        val preSettlementPairingApprove = proposal.toApprove(generateId(), preSettledPairing.expiry, selfPublicKey)
        relayer.isConnectionOpened
            .filter { isOnline -> isOnline }
            .onEach {
                supervisorScope {
                    relayer.publishJsonRpcRequests(proposal.topic, preSettlementPairingApprove) { result ->
                        result.fold(
                            onSuccess = { onPairingSuccess(proposal, preSettledPairing, onSuccess) },
                            onFailure = { throwable ->
                                Logger.error("Pairing approve error: $throwable")
                                onFailure(throwable)
                            }
                        )
                    }
                    cancel()
                }
            }.launchIn(scope)
    }

    private fun onPairingSuccess(proposal: PairingParamsVO.Proposal, sequence: PairingVO, onSuccess: (String) -> Unit) {
        sequenceStorageRepository.updatePreSettledPairingToAcknowledged(sequence.copy(status = SequenceStatus.ACKNOWLEDGED))
        relayer.unsubscribe(proposal.topic)
        onSuccess(sequence.topic.value)
        pairingUpdate(sequence)
    }

    private fun pairingUpdate(settledSequence: PairingVO) {
        val params = PairingParamsVO.UpdateParams(state = PairingStateVO(metaData.toMetaDataVO()))
        val pairingUpdate: PostSettlementPairingVO.PairingUpdate = PostSettlementPairingVO.PairingUpdate(id = generateId(), params = params)
        relayer.publishJsonRpcRequests(settledSequence.topic, pairingUpdate) { result ->
            result.fold(
                onSuccess = {
                    sequenceStorageRepository.updateAcknowledgedPairingMetadata(metaData.toMetaDataVO(), settledSequence.topic)
                    Logger.log("Responder; Pairing update success")
                },
                onFailure = { error -> Logger.error("Pairing update error: $error") }
            )
        }
    }

    internal fun approve(proposal: EngineDO.SessionProposal, onSuccess: (EngineDO.SettledSession) -> Unit, onFailure: (Throwable) -> Unit) {
        checkPeer(ControllerType.CONTROLLER) {
            throw WalletConnectException.UnauthorizedPeerException(UNAUTHORIZED_APPROVE_MESSAGE)
        }

        Validator.validateProposalFields(proposal) { errorMessage ->
            throw WalletConnectException.InvalidSessionProposalException(errorMessage)
        }

        if (!isSequenceValid(TopicVO(proposal.topic))) {
            throw WalletConnectException.CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE${proposal.topic}")
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
        val respondedSession = proposal.toRespondedSessionVO(selfPublicKey, controllerType)
        val preSettledSession = proposal.toPreSettledSessionVO(settledTopic, selfPublicKey, controllerType)

        sequenceStorageRepository.updateProposedSessionToResponded(respondedSession)
        relayer.subscribe(TopicVO(proposal.topic))

        relayer.subscribe(settledTopic)
        sequenceStorageRepository.updateRespondedSessionToPreSettled(preSettledSession, TopicVO(proposal.topic))

        val params = SessionParamsVO.ApprovalParams(
            relay = RelayProtocolOptionsVO(),
            state = SessionStateVO(proposal.accounts),
            expiry = preSettledSession.expiry,
            responder = SessionParticipantVO(selfPublicKey.keyAsHex, metadata = metaData.toMetaDataVO())
        )
        val sessionApprove = PreSettlementSessionVO.Approve(id = generateId(), params = params)
        relayer.publishJsonRpcRequests(TopicVO(proposal.topic), sessionApprove) { result ->
            result.fold(
                onSuccess = {
                    relayer.unsubscribe(TopicVO(proposal.topic))
                    crypto.removeKeys(proposal.topic)
                    sequenceStorageRepository.updatePreSettledSessionToAcknowledged(preSettledSession.copy(status = SequenceStatus.ACKNOWLEDGED))
                    onSuccess(proposal.toEngineDOSettledSessionVO(settledTopic, preSettledSession.expiry))
                },
                onFailure = { error ->
                    Logger.error("Session approve error: $error")
                    relayer.unsubscribe(TopicVO(proposal.topic))
                    relayer.unsubscribe(settledTopic)
                    crypto.removeKeys(proposal.topic)
                    crypto.removeKeys(settledTopic.value)
                    sequenceStorageRepository.deleteSession(settledTopic)
                    onFailure(error)
                }
            )
        }
    }

    internal fun reject(reason: String, topic: String, onSuccess: (Pair<String, String>) -> Unit, onFailure: (Throwable) -> Unit) {
        checkPeer(ControllerType.CONTROLLER) {
            throw WalletConnectException.UnauthorizedPeerException(UNAUTHORIZED_REJECT_MESSAGE)
        }

        if (!isSequenceValid(TopicVO(topic))) {
            throw WalletConnectException.CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE$topic")
        }

        val params = SessionParamsVO.RejectParams(reason = ReasonVO(message = reason))
        val sessionReject = PreSettlementSessionVO.Reject(id = generateId(), params = params)

        sequenceStorageRepository.deleteSession(TopicVO(topic))
        onSuccess(Pair(topic, reason))
        relayer.publishJsonRpcRequests(TopicVO(topic), sessionReject) { result ->
            result.fold(
                onSuccess = {
                    crypto.removeKeys(topic)
                    relayer.unsubscribe(TopicVO(topic))
                },
                onFailure = { error ->
                    Logger.error("Session reject error: $error")
                    onFailure(error)
                }
            )
        }
    }

    internal fun upgrade(
        topic: String, permissions: EngineDO.SessionPermissions,
        onSuccess: (Pair<String, EngineDO.SessionPermissions>) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
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
        relayer.publishJsonRpcRequests(TopicVO(topic), sessionUpgrade) { result ->
            result.fold(
                onSuccess = { onSuccess(Pair(topic, permissions)) },
                onFailure = { error ->
                    Logger.error("Session upgrade error: $error")
                    onFailure(error)
                }
            )
        }
    }

    internal fun update(
        topic: String,
        state: EngineDO.SessionState,
        onSuccess: (Pair<String, List<String>>) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
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
        relayer.publishJsonRpcRequests(TopicVO(topic), sessionUpdate) { result ->
            result.fold(
                onSuccess = { onSuccess(Pair(topic, state.accounts)) },
                onFailure = { error ->
                    Logger.error("Session update error: $error")
                    onFailure(error)
                }
            )
        }
    }

    internal fun sessionRequest(
        request: EngineDO.Request,
        onSuccess: (EngineDO.JsonRpcResponse.JsonRpcResult) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        if (!isSequenceValid(TopicVO(request.topic))) {
            throw WalletConnectException.CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE${request.topic}")
        }

        val chains: List<String> = sequenceStorageRepository.getSessionByTopic(TopicVO(request.topic)).chains
        Validator.validateChainIdAuthorization(request.chainId, chains) { errorMessage ->
            throw WalletConnectException.UnauthorizedChainIdException(errorMessage)
        }

        val params = SessionParamsVO.SessionPayloadParams(request = SessionRequestVO(request.method, request.params), chainId = request.chainId)
        val sessionPayload = PostSettlementSessionVO.SessionPayload(id = generateId(), params = params)

        val timer = startTimer(FIVE_MINUTES_TIMEOUT, onFailure)
        relayer.publishJsonRpcRequests(TopicVO(request.topic), sessionPayload) { result ->
            result.fold(
                onSuccess = { jsonRpcResult ->
                    timer.reset()
                    onSuccess(jsonRpcResult.toEngineJsonRpcResult())
                },
                onFailure = { error ->
                    timer.reset()
                    Logger.error("Session request error: $error")
                    onFailure(error)
                }
            )
        }
    }

    internal fun respondSessionPayload(topic: String, jsonRpcResponse: JsonRpcResponseVO, onFailure: (Throwable) -> Unit) {
        if (!isSequenceValid(TopicVO(topic))) {
            throw WalletConnectException.CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE$topic")
        }

        relayer.publishJsonRpcResponse(TopicVO(topic), jsonRpcResponse,
            { Logger.error("Session payload sent successfully") },
            { error ->
                onFailure(error)
                Logger.error("Sending session payload error: $error")
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

        val timer = startTimer(THIRTY_SECONDS_TIMEOUT, onFailure)
        relayer.publishJsonRpcRequests(TopicVO(topic), pingParams) { result ->
            result.fold(
                onSuccess = {
                    timer.reset()
                    Logger.error("Kobe; Success; Cancel timeout")
                    onSuccess(topic)
                },
                onFailure = { error ->
                    timer.reset()
                    Logger.error("Kobe; Error; Cancel timeout")
                    onFailure(error)
                }
            )
        }
    }

    internal fun notify(topic: String, notification: EngineDO.Notification, onSuccess: (String) -> Unit, onFailure: (Throwable) -> Unit) {
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
        relayer.publishJsonRpcRequests(TopicVO(topic), sessionNotification) { result ->
            result.fold(
                onSuccess = { onSuccess(topic) },
                onFailure = { error ->
                    Logger.error("Notify error: $error")
                    onFailure(error)
                }
            )
        }
    }

    internal fun disconnect(
        topic: String,
        reason: String,
        code: Int,
        onSuccess: (Pair<String, String>) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        if (!isSequenceValid(TopicVO(topic))) {
            throw WalletConnectException.CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE$topic")
        }

        val deleteParams = SessionParamsVO.DeleteParams(ReasonVO(message = reason, code = code))
        val sessionDelete = PostSettlementSessionVO.SessionDelete(id = generateId(), params = deleteParams)
        sequenceStorageRepository.deleteSession(TopicVO(topic))
        relayer.unsubscribe(TopicVO(topic))
        onSuccess(Pair(topic, reason))
        relayer.publishJsonRpcRequests(TopicVO(topic), sessionDelete) { result ->
            result.fold(
                onSuccess = {/*TODO: Should wait for acknowledgement and delete keys?*/ },
                onFailure = { error ->
                    Logger.error("Session disconnect error: $error")
                    onFailure(error)
                }
            )
        }
    }

    internal fun getListOfPendingSessions(): List<EngineDO.SessionProposal> {
        return sequenceStorageRepository.getListOfSessionVOs()
            .filter { session -> session.status == SequenceStatus.PROPOSED && session.expiry.isSequenceValid() }
            .map { session -> session.toEngineDOSessionProposal(crypto.getKeyAgreement(session.topic).second as PublicKey) }
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

    internal fun getListOfJsonRpcHistory(topic: TopicVO): Pair<List<JsonRpcHistoryVO>, List<JsonRpcHistoryVO>> {
        return relayer.getJsonRpcHistory(topic)
    }

    private fun collectClientSyncJsonRpc() {
        scope.launch {
            relayer.clientSyncJsonRpc.collect { payload ->
                when (val requestParams = payload.params) {
                    is PairingParamsVO.PayloadParams -> onPairingPayload(payload.request, requestParams)
                    is PairingParamsVO.ApproveParams -> onPairingApprove(payload.request, requestParams)
                    is PairingParamsVO.DeleteParams -> onPairingDelete(payload.request, requestParams)
                    is PairingParamsVO.UpdateParams -> onPairingUpdate(payload.request, requestParams)
                    is SessionParamsVO.ApprovalParams -> onSessionApprove(payload.request, requestParams)
                    is SessionParamsVO.RejectParams -> onSessionReject(payload.request, requestParams)
                    is SessionParamsVO.DeleteParams -> onSessionDelete(payload.request, requestParams)
                    is SessionParamsVO.SessionPayloadParams -> onSessionPayload(payload.request, requestParams)
                    is SessionParamsVO.UpdateParams -> onSessionUpdate(payload.request, requestParams)
                    is SessionParamsVO.UpgradeParams -> onSessionUpgrade(payload.request, requestParams)
                    is SessionParamsVO.NotificationParams -> onSessionNotification(payload.request, requestParams)
                    is SessionParamsVO.PingParams, is PairingParamsVO.PingParams -> onPing(payload.request)
                }
            }
        }
    }

    private fun onPairingPayload(request: WCRequestVO, payloadParams: PairingParamsVO.PayloadParams) {
        if (!isSequenceValid(request.topic)) {
            relayer.respondWithError(request, PeerError(Error.NoMatchingTopic(Sequence.PAIRING.name, request.topic.value)))
            return
        }

        if (request.method != JsonRpcMethod.WC_SESSION_PROPOSE) {
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
        val proposedSession = proposal.toEngineDOSettledSessionVO(publicKey as PublicKey, controllerType)
        sequenceStorageRepository.insertSessionProposal(proposedSession, proposal.proposer.metadata, controllerType)
        crypto.setEncryptionKeys(sharedKey as SharedKey, publicKey, proposal.topic)

        relayer.respondWithSuccess(request)
        _sequenceEvent.value = payloadParams.toEngineDOSessionProposal()
    }

    private fun onPairingApprove(request: WCRequestVO, params: PairingParamsVO.ApproveParams) {
        if (!isSequenceValid(request.topic)) {
            relayer.respondWithError(request, PeerError(Error.NoMatchingTopic(Sequence.PAIRING.name, request.topic.value)))
            return
        }

        val pendingPairing: PairingVO = sequenceStorageRepository.getPairingByTopic(request.topic)
        if (pendingPairing.status != SequenceStatus.PROPOSED) {
            relayer.respondWithError(request, PeerError(Error.NoMatchingTopic(Sequence.PAIRING.name, request.topic.value)))
            return
        }

        val (_, settledTopic) = crypto.generateTopicAndSharedKey(pendingPairing.selfParticipant, PublicKey(params.responder.publicKey))
        val acknowledgedPairing = pendingPairing.toAcknowledgedPairingVO(settledTopic, params, controllerType)
        sequenceStorageRepository.updateProposedPairingToAcknowledged(acknowledgedPairing, request.topic)

        relayer.subscribe(settledTopic)
        relayer.unsubscribe(pendingPairing.topic)

        if (!this::sessionPermissions.isInitialized) {
            Logger.error("Cannot find permissions for pending session")
            return
        }

        relayer.respondWithSuccess(request)
        proposeSession(sessionPermissions, settledTopic.value)
        _sequenceEvent.value = acknowledgedPairing.toEngineDOSettledPairing()
    }

    private fun onPairingDelete(request: WCRequestVO, params: PairingParamsVO.DeleteParams) {
        if (!isSequenceValid(request.topic)) {
            relayer.respondWithError(request, PeerError(Error.NoMatchingTopic(Sequence.PAIRING.name, request.topic.value)))
            return
        }

        crypto.removeKeys(request.topic.value)
        relayer.unsubscribe(request.topic)
        sequenceStorageRepository.deletePairing(request.topic)
        _sequenceEvent.value = EngineDO.DeletedPairing(request.topic.value, params.reason.message)
    }

    private fun onPairingUpdate(request: WCRequestVO, params: PairingParamsVO.UpdateParams) {
        if (!isSequenceValid(request.topic)) {
            relayer.respondWithError(request, PeerError(Error.NoMatchingTopic(Sequence.PAIRING.name, request.topic.value)))
            return
        }

        if (params.state.metadata == null) {
            relayer.respondWithError(request, PeerError(Error.InvalidUpdateRequest(Sequence.PAIRING.name)))
            return
        }

        val pairing: PairingVO = sequenceStorageRepository.getPairingByTopic(request.topic)
        if (!pairing.isPeerController) {
            relayer.respondWithError(request, PeerError(Error.UnauthorizedUpdateRequest(Sequence.PAIRING.name)))
            return
        }

        sequenceStorageRepository.updateAcknowledgedPairingMetadata(params.state.metadata, pairing.topic)
        relayer.respondWithSuccess(request)
        _sequenceEvent.value = EngineDO.PairingUpdate(request.topic, params.state.metadata.toEngineDOMetaData())
    }

    private fun onSessionApprove(request: WCRequestVO, params: SessionParamsVO.ApprovalParams) {
        if (!isSequenceValid(request.topic)) {
            relayer.respondWithError(request, PeerError(Error.NoMatchingTopic(Sequence.SESSION.name, request.topic.value)))
            return
        }

        if (controllerType != ControllerType.NON_CONTROLLER) {
            relayer.respondWithError(request, PeerError(Error.UnauthorizedMatchingController(true)))
            return
        }

        val pendingSession: SessionVO = sequenceStorageRepository.getSessionByTopic(request.topic)
        if (pendingSession.status != SequenceStatus.PROPOSED) {
            relayer.respondWithError(request, PeerError(Error.NoMatchingTopic(Sequence.SESSION.name, request.topic.value)))
            return
        }

        val (_, settledTopic) = crypto.generateTopicAndSharedKey(pendingSession.selfParticipant, PublicKey(params.responder.publicKey))
        val acknowledgedSession = pendingSession.toEngineDOSettledSessionVO(settledTopic, params)
        sequenceStorageRepository.updateProposedSessionToAcknowledged(acknowledgedSession, pendingSession.topic)

        relayer.subscribe(settledTopic)
        relayer.unsubscribe(pendingSession.topic)
        relayer.respondWithSuccess(request)
        _sequenceEvent.value = pendingSession.toSessionApproved(params, settledTopic)
    }

    private fun onSessionReject(request: WCRequestVO, params: SessionParamsVO.RejectParams) {
        if (!isSequenceValid(request.topic)) {
            relayer.respondWithError(request, PeerError(Error.NoMatchingTopic(Sequence.SESSION.name, request.topic.value)))
            return
        }

        sequenceStorageRepository.deleteSession(request.topic)
        relayer.unsubscribe(request.topic)
        relayer.respondWithSuccess(request)
        _sequenceEvent.value = EngineDO.SessionRejected(request.topic.value, params.reason.message)
    }

    private fun onSessionDelete(request: WCRequestVO, params: SessionParamsVO.DeleteParams) {
        if (!isSequenceValid(request.topic)) {
            relayer.respondWithError(request, PeerError(Error.NoMatchingTopic(Sequence.SESSION.name, request.topic.value)))
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
            relayer.respondWithError(request, PeerError(Error.NoMatchingTopic(Sequence.SESSION.name, request.topic.value)))
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
            relayer.respondWithError(request, PeerError(Error.NoMatchingTopic(Sequence.SESSION.name, request.topic.value)))
            return
        }

        val session: SessionVO = sequenceStorageRepository.getSessionByTopic(request.topic)
        if (!session.isPeerController) {
            relayer.respondWithError(request, PeerError(Error.UnauthorizedUpdateRequest(Sequence.SESSION.name)))
            return
        }

        Validator.validateCAIP10(params.state.accounts) {
            relayer.respondWithError(request, PeerError(Error.InvalidUpdateRequest(Sequence.SESSION.name)))
            return@validateCAIP10
        }

        sequenceStorageRepository.updateSessionWithAccounts(session.topic, params.state.accounts)
        relayer.respondWithSuccess(request)
        _sequenceEvent.value = EngineDO.SessionUpdate(request.topic, params.state.accounts)
    }

    private fun onSessionUpgrade(request: WCRequestVO, params: SessionParamsVO.UpgradeParams) {
        if (!isSequenceValid(request.topic)) {
            relayer.respondWithError(request, PeerError(Error.NoMatchingTopic(Sequence.SESSION.name, request.topic.value)))
            return
        }

        val session: SessionVO = sequenceStorageRepository.getSessionByTopic(request.topic)
        if (!session.isPeerController) {
            relayer.respondWithError(request, PeerError(Error.UnauthorizedUpgradeRequest(Sequence.SESSION.name)))
            return
        }

        val chains = params.permissions.blockchain.chains
        val methods = params.permissions.jsonRpc.methods

        Validator.validateSessionPermissions(params.permissions.toEngineDOPermissions()) {
            relayer.respondWithError(request, PeerError(Error.InvalidUpgradeRequest(Sequence.SESSION.name)))
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
            relayer.respondWithError(request, PeerError(Error.NoMatchingTopic(Sequence.SESSION.name, request.topic.value)))
            return
        }

        val session = sequenceStorageRepository.getSessionByTopic(request.topic)
        if (session.status != SequenceStatus.ACKNOWLEDGED) {
            relayer.respondWithError(request, PeerError(Error.NoMatchingTopic(Sequence.SESSION.name, request.topic.value)))
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
            relayer.respondWithError(request, PeerError(Error.NoMatchingTopic(Sequence.SESSION.name, request.topic.value)))
            return
        }

        relayer.respondWithSuccess(request)
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
        const val THIRTY_SECONDS_TIMEOUT: Int = 30
        const val FIVE_MINUTES_TIMEOUT: Int = 300
    }
}