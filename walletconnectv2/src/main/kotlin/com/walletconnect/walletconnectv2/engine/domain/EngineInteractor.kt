package com.walletconnect.walletconnectv2.engine.domain

import com.walletconnect.walletconnectv2.core.exceptions.client.*
import com.walletconnect.walletconnectv2.core.exceptions.peer.Error
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
import com.walletconnect.walletconnectv2.util.Logger
import com.walletconnect.walletconnectv2.util.bytesToHex
import com.walletconnect.walletconnectv2.util.generateId
import com.walletconnect.walletconnectv2.util.randomBytes
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
            checkTopic(TopicVO(pairingTopic), "$NO_SEQUENCE_FOR_TOPIC_MESSAGE$pairingTopic") { message ->
                throw WalletConnectException.CannotFindSequenceForTopic(message)
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

        if (sequenceStorageRepository.hasPairingTopic(walletConnectUri.topic)) {
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
                            onFailure = { throwable -> onFailure(throwable) }
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

        checkTopic(TopicVO(proposal.topic), "$NO_SEQUENCE_FOR_TOPIC_MESSAGE${proposal.topic}") { message ->
            throw WalletConnectException.CannotFindSequenceForTopic(message)
        }

        Validator.validateAccounts(proposal.accounts, proposal.chains) { errorMessage ->
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

        checkTopic(TopicVO(topic), "$NO_SEQUENCE_FOR_TOPIC_MESSAGE$topic") { message ->
            throw WalletConnectException.CannotFindSequenceForTopic(message)
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
                onFailure = { error -> onFailure(error) }
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

        checkTopic(TopicVO(topic), "$NO_SEQUENCE_FOR_TOPIC_MESSAGE$topic") { message ->
            throw WalletConnectException.CannotFindSequenceForTopic(message)
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
                onFailure = { error -> onFailure(error) }
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

        checkTopic(TopicVO(topic), "$NO_SEQUENCE_FOR_TOPIC_MESSAGE$topic") { message ->
            throw WalletConnectException.CannotFindSequenceForTopic(message)
        }

        val chains: List<String> = sequenceStorageRepository.getSessionByTopic(TopicVO(topic)).chains
        Validator.validateAccounts(state.accounts, chains) { errorMessage ->
            throw WalletConnectException.InvalidAccountsException(errorMessage)
        }

        val params = SessionParamsVO.UpdateParams(SessionStateVO(state.accounts))
        val sessionUpdate: PostSettlementSessionVO.SessionUpdate = PostSettlementSessionVO.SessionUpdate(id = generateId(), params = params)
        sequenceStorageRepository.updateSessionWithAccounts(TopicVO(topic), state.accounts)
        relayer.publishJsonRpcRequests(TopicVO(topic), sessionUpdate) { result ->
            result.fold(
                onSuccess = { onSuccess(Pair(topic, state.accounts)) },
                onFailure = { error -> onFailure(error) }
            )
        }
    }

    internal fun sessionRequest(
        request: EngineDO.Request,
        onSuccess: (EngineDO.JsonRpcResponse.JsonRpcResult) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        checkTopic(TopicVO(request.topic), "$NO_SEQUENCE_FOR_TOPIC_MESSAGE${request.topic}") { message ->
            throw WalletConnectException.CannotFindSequenceForTopic(message)
        }

        val chains: List<String> = sequenceStorageRepository.getSessionByTopic(TopicVO(request.topic)).chains
        Validator.validateChainIdAuthorization(request.chainId, chains) { errorMessage ->
            throw WalletConnectException.UnauthorizedChainIdException(errorMessage)
        }

        //TODO: Add timeout validation for peer response - 5s
        val params = SessionParamsVO.SessionPayloadParams(request = SessionRequestVO(request.method, request.params), chainId = request.chainId)
        val sessionPayload = PostSettlementSessionVO.SessionPayload(id = generateId(), params = params)
        relayer.publishJsonRpcRequests(TopicVO(request.topic), sessionPayload) { result ->
            result.fold(
                onSuccess = { jsonRpcResult -> onSuccess(jsonRpcResult.toEngineJsonRpcResult()) },
                onFailure = { error -> onFailure(error) }
            )
        }
    }

    internal fun respondSessionPayload(topic: String, jsonRpcResponse: JsonRpcResponseVO, onFailure: (Throwable) -> Unit) {
        checkTopic(TopicVO(topic), "$NO_SEQUENCE_FOR_TOPIC_MESSAGE$topic") { message ->
            throw WalletConnectException.CannotFindSequenceForTopic(message)
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
            sequenceStorageRepository.hasSessionTopic(TopicVO(topic)) ->
                PostSettlementSessionVO.SessionPing(id = generateId(), params = SessionParamsVO.PingParams())
            sequenceStorageRepository.hasPairingTopic(TopicVO(topic)) ->
                PostSettlementPairingVO.PairingPing(id = generateId(), params = PairingParamsVO.PingParams())
            else -> throw WalletConnectException.CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE$topic")
        }

        //TODO: Add timeout validation for peer response - 30s
        relayer.publishJsonRpcRequests(TopicVO(topic), pingParams) { result ->
            result.fold(
                onSuccess = { onSuccess(topic) },
                onFailure = { error -> onFailure(error) }
            )
        }
    }

    internal fun notify(topic: String, notification: EngineDO.Notification, onSuccess: (String) -> Unit, onFailure: (Throwable) -> Unit) {
        checkTopic(TopicVO(topic), "$NO_SEQUENCE_FOR_TOPIC_MESSAGE$topic") { message ->
            throw WalletConnectException.CannotFindSequenceForTopic(message)
        }

        //TODO: check the Notification authorization: of types is included and check public key
        Validator.validateNotification(notification) { errorMessage ->
            throw WalletConnectException.InvalidNotificationException(errorMessage)
        }

        val notificationParams = SessionParamsVO.NotificationParams(notification.type, notification.data)
        val sessionNotification = PostSettlementSessionVO.SessionNotification(id = generateId(), params = notificationParams)
        relayer.publishJsonRpcRequests(TopicVO(topic), sessionNotification) { result ->
            result.fold(
                onSuccess = { onSuccess(topic) },
                onFailure = { error -> onFailure(error) }
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
        checkTopic(TopicVO(topic), "$NO_SEQUENCE_FOR_TOPIC_MESSAGE$topic") { message ->
            throw WalletConnectException.CannotFindSequenceForTopic(message)
        }

        val deleteParams = SessionParamsVO.DeleteParams(ReasonVO(message = reason, code = code))
        val sessionDelete = PostSettlementSessionVO.SessionDelete(id = generateId(), params = deleteParams)
        sequenceStorageRepository.deleteSession(TopicVO(topic))
        relayer.unsubscribe(TopicVO(topic))
        onSuccess(Pair(topic, reason))
        relayer.publishJsonRpcRequests(TopicVO(topic), sessionDelete) { result ->
            result.fold(
                onSuccess = {/*TODO: Should wait for acknowledgement and delete keys?*/ },
                onFailure = { error -> onFailure(error) }
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
                when (payload.params) {
                    is PairingParamsVO.PayloadParams -> onPairingPayload(payload.params, payload.topic, payload.requestId)
                    is PairingParamsVO.ApproveParams -> onPairingApprove(payload.params, payload.topic, payload.requestId)
                    is PairingParamsVO.DeleteParams -> onPairingDelete(payload.params, payload.topic, payload.requestId)
                    is PairingParamsVO.UpdateParams -> onPairingUpdate(payload.params, payload.topic, payload.requestId)
                    is SessionParamsVO.ApprovalParams -> onSessionApprove(payload.params, payload.topic, payload.requestId)
                    is SessionParamsVO.RejectParams -> onSessionReject(payload.params, payload.topic, payload.requestId)
                    is SessionParamsVO.DeleteParams -> onSessionDelete(payload.params, payload.topic, payload.requestId)
                    is SessionParamsVO.SessionPayloadParams -> onSessionPayload(payload.params, payload.topic, payload.requestId)
                    is SessionParamsVO.UpdateParams -> onSessionUpdate(payload.params, payload.topic, payload.requestId)
                    is SessionParamsVO.UpgradeParams -> onSessionUpgrade(payload.params, payload.topic, payload.requestId)
                    is SessionParamsVO.NotificationParams -> onSessionNotification(payload.params, payload.topic, payload.requestId)
                    is PairingParamsVO.PingParams, is SessionParamsVO.PingParams -> onPing(payload.topic, payload.requestId)
                }
            }
        }
    }

    private fun onPairingPayload(payload: PairingParamsVO.PayloadParams, topic: TopicVO, requestId: Long) {
        checkTopic(topic, "${Error.NO_MATCHING_PAIRING_TOPIC.message}$topic") { errorMessage ->
            respondWithError(requestId, topic, errorMessage, Error.NO_MATCHING_PAIRING_TOPIC.code)
            return@checkTopic
        }

        if (payload.request.method != JsonRpcMethod.WC_SESSION_PROPOSE) {
            Logger.error("Forbidden PairingPayload method")
            return
        }

        val proposal: SessionParamsVO.ProposalParams = payload.request.params
        val isController: Boolean = controllerType == ControllerType.CONTROLLER
        if (proposal.proposer.controller == isController) {
            val peer: String = if (isController) "controller" else "non-controller"
            val message = "${Error.UNAUTHORIZED_MATCHING_CONTROLLER.message}$peer"
            val code = Error.UNAUTHORIZED_MATCHING_CONTROLLER.code
            respondWithError(requestId, topic, message, code)
            return
        }

        val (sharedKey, publicKey) = crypto.getKeyAgreement(proposal.signal.params.topic)
        val proposedSession = proposal.toEngineDOSettledSessionVO(publicKey as PublicKey, controllerType)
        sequenceStorageRepository.insertSessionProposal(proposedSession, proposal.proposer.metadata, controllerType)
        crypto.setEncryptionKeys(sharedKey as SharedKey, publicKey, proposal.topic)

        val jsonRpcResult = EngineDO.JsonRpcResponse.JsonRpcResult(id = requestId, result = "true")
        relayer.publishJsonRpcResponse(topic, response = jsonRpcResult.toJsonRpcResult(),
            onFailure = { error -> Logger.error("onPairingPayload: Cannot send the respond, error: $error") })

        _sequenceEvent.value = payload.toEngineDOSessionProposal()
    }

    private fun onPairingApprove(params: PairingParamsVO.ApproveParams, topic: TopicVO, requestId: Long) {
        checkTopic(topic, "${Error.NO_MATCHING_PAIRING_TOPIC.message}$topic") { errorMessage ->
            respondWithError(requestId, topic, errorMessage, Error.NO_MATCHING_PAIRING_TOPIC.code)
            return@checkTopic
        }

        val pendingPairing: PairingVO = sequenceStorageRepository.getPairingByTopic(topic)
        if (pendingPairing.status != SequenceStatus.PROPOSED) {
            respondWithError(requestId, topic, "${Error.NO_MATCHING_PAIRING_TOPIC.message}$topic", Error.NO_MATCHING_PAIRING_TOPIC.code)
            return
        }

        val (_, settledTopic) = crypto.generateTopicAndSharedKey(pendingPairing.selfParticipant, PublicKey(params.responder.publicKey))
        val acknowledgedPairing = pendingPairing.toAcknowledgedPairingVO(settledTopic, params, controllerType)
        sequenceStorageRepository.updateProposedPairingToAcknowledged(acknowledgedPairing, topic)

        relayer.subscribe(settledTopic)
        relayer.unsubscribe(pendingPairing.topic)

        if (!this::sessionPermissions.isInitialized) {
            Logger.error("Cannot find permissions for pending session")
            return
        }

        val jsonRpcResult = EngineDO.JsonRpcResponse.JsonRpcResult(id = requestId, result = "true")
        relayer.publishJsonRpcResponse(pendingPairing.topic, jsonRpcResult.toJsonRpcResult(),
            onFailure = { error -> Logger.error("onPairingApproved: Cannot send the respond, error: $error") })

        proposeSession(sessionPermissions, settledTopic.value)
        _sequenceEvent.value = acknowledgedPairing.toEngineDOSettledPairing()
    }

    private fun onPairingDelete(params: PairingParamsVO.DeleteParams, topic: TopicVO, requestId: Long) {
        checkTopic(topic, "${Error.NO_MATCHING_PAIRING_TOPIC.message}$topic") { errorMessage ->
            respondWithError(requestId, topic, errorMessage, Error.NO_MATCHING_PAIRING_TOPIC.code)
            return@checkTopic
        }

        crypto.removeKeys(topic.value)
        relayer.unsubscribe(topic)
        sequenceStorageRepository.deletePairing(topic)
        _sequenceEvent.value = EngineDO.DeletedPairing(topic.value, params.reason.message)
    }

    private fun onPairingUpdate(params: PairingParamsVO.UpdateParams, topic: TopicVO, requestId: Long) {
        checkTopic(topic, "${Error.NO_MATCHING_PAIRING_TOPIC.message}$topic") { errorMessage ->
            respondWithError(requestId, topic, errorMessage, Error.NO_MATCHING_PAIRING_TOPIC.code)
            return@checkTopic
        }

        val pairing: PairingVO = sequenceStorageRepository.getPairingByTopic(topic)
        if (pairing.controllerType != ControllerType.NON_CONTROLLER) {
            val message = Error.UNAUTHORIZED_PAIRING_UPDATE.message
            val code = Error.UNAUTHORIZED_PAIRING_UPDATE.code
            respondWithError(requestId, topic, message, code)
            return
        }

        sequenceStorageRepository.updateAcknowledgedPairingMetadata(params.state.metadata, pairing.topic)
        val jsonRpcResult = EngineDO.JsonRpcResponse.JsonRpcResult(id = requestId, result = "true")
        relayer.publishJsonRpcResponse(topic, jsonRpcResult.toJsonRpcResult(),
            onFailure = { error -> Logger.error("onPairingUpdate:Cannot send the respond, error: $error") })

        _sequenceEvent.value = EngineDO.PairingUpdate(topic, params.state.metadata.toEngineDOMetaData())
    }

    private fun onSessionApprove(params: SessionParamsVO.ApprovalParams, topic: TopicVO, requestId: Long) {
        checkTopic(topic, "${Error.NO_MATCHING_SESSION_TOPIC.message}$topic") { errorMessage ->
            respondWithError(requestId, topic, errorMessage, Error.NO_MATCHING_SESSION_TOPIC.code)
            return@checkTopic
        }

        if (controllerType != ControllerType.CONTROLLER) {
            val peer: String = if (controllerType == ControllerType.CONTROLLER) "controller" else "non-controller"
            val message = "${Error.UNAUTHORIZED_MATCHING_CONTROLLER.message}$peer"
            val code = Error.UNAUTHORIZED_MATCHING_CONTROLLER.code
            respondWithError(requestId, topic, message, code)
            return
        }

        val pendingSession: SessionVO = sequenceStorageRepository.getSessionByTopic(topic)
        if (pendingSession.status != SequenceStatus.PROPOSED) {
            Logger.error("No pending session for topic: $topic")
            return
        }

        val (_, settledTopic) = crypto.generateTopicAndSharedKey(pendingSession.selfParticipant, PublicKey(params.responder.publicKey))
        val acknowledgedSession = pendingSession.toEngineDOSettledSessionVO(settledTopic, params)
        sequenceStorageRepository.updateProposedSessionToAcknowledged(acknowledgedSession, pendingSession.topic)

        relayer.subscribe(settledTopic)
        relayer.unsubscribe(pendingSession.topic)

        val jsonRpcResult = EngineDO.JsonRpcResponse.JsonRpcResult(id = requestId, result = "true")
        relayer.publishJsonRpcResponse(topic, jsonRpcResult.toJsonRpcResult(),
            onFailure = { error -> Logger.error("onSessionApproved: Cannot send the respond, error: $error") })

        _sequenceEvent.value = pendingSession.toSessionApproved(params, settledTopic)
    }

    private fun onSessionReject(params: SessionParamsVO.RejectParams, topic: TopicVO, requestId: Long) {
        checkTopic(topic, "${Error.NO_MATCHING_SESSION_TOPIC.message}$topic") { errorMessage ->
            respondWithError(requestId, topic, errorMessage, Error.NO_MATCHING_SESSION_TOPIC.code)
            return@checkTopic
        }

        sequenceStorageRepository.deleteSession(topic)
        relayer.unsubscribe(topic)
        _sequenceEvent.value = EngineDO.SessionRejected(topic.value, params.reason.message)
    }

    private fun onSessionDelete(params: SessionParamsVO.DeleteParams, topic: TopicVO, requestId: Long) {
        checkTopic(topic, "${Error.NO_MATCHING_SESSION_TOPIC.message}$topic") { errorMessage ->
            respondWithError(requestId, topic, errorMessage, Error.NO_MATCHING_SESSION_TOPIC.code)
            return@checkTopic
        }

        crypto.removeKeys(topic.value)
        sequenceStorageRepository.deleteSession(topic)
        relayer.unsubscribe(topic)
        _sequenceEvent.value = params.toEngineDoDeleteSession(topic)
    }

    private fun onSessionPayload(params: SessionParamsVO.SessionPayloadParams, topic: TopicVO, requestId: Long) {
        checkTopic(topic, "${Error.NO_MATCHING_SESSION_TOPIC.message}$topic") { errorMessage ->
            respondWithError(requestId, topic, errorMessage, Error.NO_MATCHING_SESSION_TOPIC.code)
            return@checkTopic
        }

        val chains = sequenceStorageRepository.getSessionByTopic(topic).chains
        if (params.chainId != null && !chains.contains(params.chainId)) {
            val message = "${Error.UNAUTHORIZED_TARGET_CHAIN_ID.message}${params.chainId}"
            val code = Error.UNAUTHORIZED_TARGET_CHAIN_ID.code
            respondWithError(requestId, topic, message, code)
            return
        }

        val methods = sequenceStorageRepository.getSessionByTopic(topic).methods
        val method = params.request.method
        if (!methods.contains(method)) {
            val message = "${Error.UNAUTHORIZED_JSON_RPC_METHOD.message}$method"
            val code = Error.UNAUTHORIZED_TARGET_CHAIN_ID.code
            respondWithError(requestId, topic, message, code)
            return
        }

        _sequenceEvent.value = params.toEngineDOSessionRequest(topic, requestId)
    }

    private fun onSessionUpdate(params: SessionParamsVO.UpdateParams, topic: TopicVO, requestId: Long) {
        checkTopic(topic, "${Error.NO_MATCHING_SESSION_TOPIC.message}$topic") { errorMessage ->
            respondWithError(requestId, topic, errorMessage, Error.NO_MATCHING_SESSION_TOPIC.code)
            return@checkTopic
        }

        val session: SessionVO = sequenceStorageRepository.getSessionByTopic(topic)
        if (!session.isPeerController) {
            respondWithError(requestId, topic, Error.UNAUTHORIZED_SESSION_UPDATE.message, Error.UNAUTHORIZED_SESSION_UPDATE.code)
            return
        }

        if (params.state.accounts.isEmpty()) {
            respondWithError(requestId, topic, Error.INVALID_SESSION_UPDATE.message, Error.INVALID_SESSION_UPDATE.code)
            return
        }

        sequenceStorageRepository.updateSessionWithAccounts(session.topic, params.state.accounts)
        val jsonRpcResult = EngineDO.JsonRpcResponse.JsonRpcResult(id = requestId, result = "true")
        relayer.publishJsonRpcResponse(topic, jsonRpcResult.toJsonRpcResult(),
            onFailure = { error -> Logger.error("onSessionUpdate: Cannot send the respond, error: $error") })

        _sequenceEvent.value = EngineDO.SessionUpdate(topic, params.state.accounts)
    }

    private fun onSessionUpgrade(params: SessionParamsVO.UpgradeParams, topic: TopicVO, requestId: Long) {
        checkTopic(topic, "${Error.NO_MATCHING_SESSION_TOPIC.message}$topic") { errorMessage ->
            respondWithError(requestId, topic, errorMessage, Error.NO_MATCHING_SESSION_TOPIC.code)
            return@checkTopic
        }

        val session: SessionVO = sequenceStorageRepository.getSessionByTopic(topic)
        if (!session.isPeerController) {
            respondWithError(requestId, topic, Error.UNAUTHORIZED_SESSION_UPGRADE.message, Error.UNAUTHORIZED_SESSION_UPGRADE.code)
            return
        }

        val chains = params.permissions.blockchain.chains
        val methods = params.permissions.jsonRpc.methods
        if (chains.isEmpty() || methods.isEmpty()) {
            respondWithError(requestId, topic, Error.INVALID_SESSION_UPGRADE.message, Error.INVALID_SESSION_UPGRADE.code)
            return
        }

        sequenceStorageRepository.upgradeSessionWithPermissions(topic, chains, methods)
        val jsonRpcResult = EngineDO.JsonRpcResponse.JsonRpcResult(id = requestId, result = "true")
        relayer.publishJsonRpcResponse(topic, jsonRpcResult.toJsonRpcResult(),
            onFailure = { error -> Logger.error("onSessionUpgrade: Cannot send the respond, error: $error") })

        _sequenceEvent.value =
            EngineDO.SessionUpgrade(session.topic, session.chains.union(chains).toList(), session.methods.union(methods).toList())
    }

    private fun onSessionNotification(params: SessionParamsVO.NotificationParams, topic: TopicVO, requestId: Long) {
        checkTopic(topic, "${Error.NO_MATCHING_SESSION_TOPIC.message}$topic") { errorMessage ->
            respondWithError(requestId, topic, errorMessage, Error.NO_MATCHING_SESSION_TOPIC.code)
            return@checkTopic
        }
        val session = sequenceStorageRepository.getSessionByTopic(topic)
        if (session.status != SequenceStatus.ACKNOWLEDGED) {
            respondWithError(requestId, topic, "${Error.NO_MATCHING_SESSION_TOPIC.message}$topic", Error.NO_MATCHING_SESSION_TOPIC.code)
            return
        }

        if (session.controllerType != ControllerType.CONTROLLER && session.types?.contains(params.type) == false) {
            val message = "${Error.UNAUTHORIZED_NOTIFICATION_TYPE.message}${params.type}"
            val code = Error.UNAUTHORIZED_NOTIFICATION_TYPE.code
            respondWithError(requestId, topic, message, code)
            return
        }

        _sequenceEvent.value = params.toEngineDoSessionNotification(topic)
    }

    private fun onPing(topic: TopicVO, requestId: Long) {
        checkTopic(topic, "${Error.NO_MATCHING_SESSION_TOPIC.message}$topic") { errorMessage ->
            respondWithError(requestId, topic, errorMessage, Error.NO_MATCHING_SESSION_TOPIC.code)
            return@checkTopic
        }

        val jsonRpcResult = EngineDO.JsonRpcResponse.JsonRpcResult(id = requestId, result = "true")
        relayer.publishJsonRpcResponse(topic, jsonRpcResult.toJsonRpcResult(),
            { Logger.log("Ping send successfully") },
            { error -> Logger.error("Ping Error: $error") })
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

    private fun respondWithError(requestId: Long, topic: TopicVO, errorMessage: String, errorCode: Int) {
        val jsonRpcError = JsonRpcResponseVO.JsonRpcError(id = requestId, error = JsonRpcResponseVO.Error(errorCode, errorMessage))
        relayer.publishJsonRpcResponse(topic, jsonRpcError,
            { Logger.log("Successfully respond with error") },
            { error -> Logger.error("Cannot respond with error: $error") })
    }

    private fun checkPeer(currentPeer: ControllerType, onUnauthorizedPeer: () -> Unit) {
        if (controllerType != currentPeer) {
            onUnauthorizedPeer()
        }
    }

    private fun <T> checkTopic(topic: TopicVO, errorMessage: String, onInvalidTopic: (String) -> T) {
        val isValid = sequenceStorageRepository.hasSessionTopic(topic) || sequenceStorageRepository.hasPairingTopic(topic)
        if (!isValid) {
            Logger.error(errorMessage)
            onInvalidTopic(errorMessage)
        }
    }

    private fun ExpiryVO.isSequenceValid(): Boolean = seconds > (System.currentTimeMillis() / 1000)

    private fun generateTopic(): TopicVO = TopicVO(randomBytes(32).bytesToHex())
}