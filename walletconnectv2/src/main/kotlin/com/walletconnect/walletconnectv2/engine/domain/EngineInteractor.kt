package com.walletconnect.walletconnectv2.engine.domain

import com.walletconnect.walletconnectv2.core.exceptions.*
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
    private var controllerType: ControllerType = ControllerType.CONTROLLER
) {
    private lateinit var sessionPermissions: EngineDO.SessionPermissions

    val sequenceEvent: StateFlow<SequenceLifecycle> =
        relayer.clientSyncJsonRpc
            .map { payload -> handleClientSyncJsonRpc(payload) }
            .stateIn(scope, SharingStarted.Lazily, EngineDO.Default)

    init {
        relayer.isConnectionOpened
            .filter { isConnected: Boolean -> isConnected }
            .onEach {
                coroutineScope {
                    launch(Dispatchers.IO) { resubscribeToSettledPairings() }
                    launch(Dispatchers.IO) { resubscribeToSettledSession() }
                }
            }
            .launchIn(scope)
    }

    internal fun proposeSequence(permissions: EngineDO.SessionPermissions, pairingTopic: String?): String? {
        checkPeer(ControllerType.NON_CONTROLLER, UNAUTHORIZED_CONNECT_MESSAGE)

        if (pairingTopic != null) {
            if (!sequenceStorageRepository.hasPairingTopic(TopicVO(pairingTopic))) {
                throw WalletConnectException.CannotFindSequenceForTopic("Topic: $pairingTopic")
            }

            //TODO: Add permissions validation
            proposeSession(permissions, pairingTopic)
            return null
        }
        return proposePairing(permissions)
    }

    private fun proposeSession(permissions: EngineDO.SessionPermissions, pairingTopic: String) {
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
                        Logger.error("Session proposal sent error: $error")
                        with(proposalParams) {
                            relayer.unsubscribe(topic)
                            sequenceStorageRepository.deleteSession(topic)
                            crypto.removeKeys(topic.value)
                        }
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
        checkPeer(ControllerType.CONTROLLER, UNAUTHORIZED_PAIR_MESSAGE)
        val proposal: PairingParamsVO.Proposal = uri.toPairProposal()
        if (sequenceStorageRepository.hasPairingTopic(proposal.topic)) throw WalletConnectException.PairWithExistingPairingIsNotAllowed

        //TODO: Add WC URI validation
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
        checkPeer(ControllerType.CONTROLLER, UNAUTHORIZED_APPROVE_MESSAGE)

        //TODO: Add SessionProposal validation
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
        checkPeer(ControllerType.CONTROLLER, UNAUTHORIZED_REJECT_MESSAGE)

        //TODO: Add error code
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

    internal fun disconnect(topic: String, reason: String, onSuccess: (Pair<String, String>) -> Unit, onFailure: (Throwable) -> Unit) {
        if (!sequenceStorageRepository.hasSessionTopic(TopicVO(topic))) throw WalletConnectException.CannotFindSequenceForTopic("Topic: $topic")

        val deleteParams = SessionParamsVO.DeleteParams(ReasonVO(message = reason))
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

    internal fun respondSessionPayload(topic: String, jsonRpcResponse: JsonRpcResponseVO, onFailure: (Throwable) -> Unit) {
        checkPeer(ControllerType.CONTROLLER, UNAUTHORIZED_RESPOND_MESSAGE)
        if (!sequenceStorageRepository.hasSessionTopic(TopicVO(topic))) throw WalletConnectException.CannotFindSequenceForTopic("Topic: $topic")

        //TODO: Add JsonRpcResponseVO validation
        relayer.publishJsonRpcResponse(
            TopicVO(topic), jsonRpcResponse,
            { Logger.error("Session payload sent successfully") },
            { error ->
                onFailure(error)
                Logger.error("Sending session payload error: $error")
            })
    }

    internal fun sessionRequest(
        request: EngineDO.Request,
        onSuccess: (EngineDO.JsonRpcResponse.JsonRpcResult) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        checkPeer(ControllerType.NON_CONTROLLER, UNAUTHORIZED_REQUEST_MESSAGE)
        val topic = TopicVO(request.topic)
        if (!sequenceStorageRepository.hasSessionTopic(topic)) throw WalletConnectException.CannotFindSequenceForTopic("Topic: $topic")

        //TODO: Add Request validation
        val params = SessionParamsVO.SessionPayloadParams(request = SessionRequestVO(request.method, request.params), chainId = request.chainId)
        val sessionPayload = PostSettlementSessionVO.SessionPayload(id = generateId(), params = params)
        relayer.publishJsonRpcRequests(topic, sessionPayload) { result ->
            result.fold(
                onSuccess = { jsonRpcResult -> onSuccess(jsonRpcResult.toEngineJsonRpcResult()) },
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
        checkPeer(ControllerType.CONTROLLER, UNAUTHORIZED_UPDATE_MESSAGE)
        if (!sequenceStorageRepository.hasSessionTopic(TopicVO(topic))) throw WalletConnectException.CannotFindSequenceForTopic("Topic: $topic")

        //TODO: Add accounts validation
        val params = SessionParamsVO.UpdateParams(SessionStateVO(state.accounts))
        val sessionUpdate: PostSettlementSessionVO.SessionUpdate = PostSettlementSessionVO.SessionUpdate(id = generateId(), params = params)
        sequenceStorageRepository.updateSessionWithAccounts(topic, state.accounts)
        relayer.publishJsonRpcRequests(TopicVO(topic), sessionUpdate) { result ->
            result.fold(
                onSuccess = { onSuccess(Pair(topic, state.accounts)) },
                onFailure = { error -> onFailure(error) }
            )
        }
    }

    internal fun upgrade(
        topic: String, permissions: EngineDO.SessionPermissions,
        onSuccess: (Pair<String, EngineDO.SessionPermissions>) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        checkPeer(ControllerType.CONTROLLER, UNAUTHORIZED_UPGRADE_MESSAGE)
        if (!sequenceStorageRepository.hasSessionTopic(TopicVO(topic))) throw WalletConnectException.CannotFindSequenceForTopic("Topic: $topic")

        //TODO: Add permissions validation
        val permissionsParams = SessionParamsVO.SessionPermissionsParams(permissions = permissions.toSessionsPermissions())
        val sessionUpgrade = PostSettlementSessionVO.SessionUpgrade(id = generateId(), params = permissionsParams)
        sequenceStorageRepository.updateSessionWithPermissions(topic, permissions.blockchain.chains, permissions.jsonRpc.methods)
        relayer.publishJsonRpcRequests(TopicVO(topic), sessionUpgrade) { result ->
            result.fold(
                onSuccess = { onSuccess(Pair(topic, permissions)) },
                onFailure = { error -> onFailure(error) }
            )
        }
    }

    internal fun notify(topic: String, notification: EngineDO.Notification, onSuccess: (String) -> Unit, onFailure: (Throwable) -> Unit) {
        if (!sequenceStorageRepository.hasSessionTopic(TopicVO(topic))) throw WalletConnectException.CannotFindSequenceForTopic("Topic: $topic")

        //TODO: Add Notification validation
        val notificationParams = SessionParamsVO.NotificationParams(notification.type, notification.data)
        val sessionNotification = PostSettlementSessionVO.SessionNotification(id = generateId(), params = notificationParams)
        relayer.publishJsonRpcRequests(TopicVO(topic), sessionNotification) { result ->
            result.fold(
                onSuccess = { onSuccess(topic) },
                onFailure = { error -> onFailure(error) }
            )
        }
    }

    internal fun ping(topic: String, onSuccess: (String) -> Unit, onFailure: (Throwable) -> Unit) {
        val pingParams = when {
            sequenceStorageRepository.hasSessionTopic(TopicVO(topic)) ->
                PostSettlementSessionVO.SessionPing(id = generateId(), params = SessionParamsVO.PingParams())
            sequenceStorageRepository.hasPairingTopic(TopicVO(topic)) ->
                PostSettlementPairingVO.PairingPing(id = generateId(), params = PairingParamsVO.PingParams())
            else -> throw WalletConnectException.CannotFindSequenceForTopic("Topic: $topic")
        }

        relayer.publishJsonRpcRequests(TopicVO(topic), pingParams) { result ->
            result.fold(
                onSuccess = { onSuccess(topic) },
                onFailure = { error -> onFailure(error) }
            )
        }
    }

    private fun handleClientSyncJsonRpc(payload: RequestSubscriptionPayloadVO) =
        when (payload.params) {
            is PairingParamsVO.ApproveParams -> onPairingApprove(payload.params, payload.topic, payload.requestId)
            is PairingParamsVO.PayloadParams -> onPairingPayload(payload.params, payload.topic, payload.requestId)
            is PairingParamsVO.DeleteParams -> onPairingDelete(payload.params, payload.topic)
            is SessionParamsVO.ApprovalParams -> onSessionApprove(payload.params, payload.topic, payload.requestId)
            is SessionParamsVO.RejectParams -> onSessionReject(payload.params, payload.topic)
            is SessionParamsVO.DeleteParams -> onSessionDelete(payload.params, payload.topic)
            is SessionParamsVO.SessionPayloadParams -> onSessionPayload(payload.params, payload.topic, payload.requestId)
            is SessionParamsVO.NotificationParams -> onSessionNotification(payload.params, payload.topic)
            is PairingParamsVO.PingParams, is SessionParamsVO.PingParams -> onPing(payload.topic, payload.requestId)
            //TODO add on pairing update to return the metadata for pairing, update pairing meta data in DB
            else -> EngineDO.Default
        }

    private fun onPairingApprove(params: PairingParamsVO.ApproveParams, pendingTopic: TopicVO, requestId: Long): SequenceLifecycle {
        //TODO: Add params validation

        if (!sequenceStorageRepository.hasPairingTopic(pendingTopic)) {
            Logger.error("onPairingApproved: No pending pairing for topic: $pendingTopic")
            return EngineDO.FailedTopic
        }

        val pendingPairing: PairingVO = sequenceStorageRepository.getPairingByTopic(pendingTopic)

        if (pendingPairing.status != SequenceStatus.PROPOSED) {
            Logger.error("onPairingApproved: No pending pairing for topic: $pendingTopic")
            return EngineDO.NoPairing
        }

        val (_, settledTopic) = crypto.generateTopicAndSharedKey(pendingPairing.selfParticipant, PublicKey(params.responder.publicKey))
        val acknowledgedPairing = pendingPairing.toAcknowledgedPairingVO(settledTopic, params, controllerType)
        sequenceStorageRepository.updateProposedPairingToAcknowledged(acknowledgedPairing, pendingTopic)

        relayer.subscribe(settledTopic)
        relayer.unsubscribe(pendingPairing.topic)

        val jsonRpcResult = EngineDO.JsonRpcResponse.JsonRpcResult(id = requestId, result = "true")
        relayer.publishJsonRpcResponse(pendingPairing.topic, jsonRpcResult.toJsonRpcResult(),
            onFailure = { error -> Logger.error("onPairingApproved: Cannot send the respond, error: $error") })

        //TODO: add permission check
        proposeSession(sessionPermissions, settledTopic.value)
        return acknowledgedPairing.toEngineDOSettledPairing()
    }

    private fun onSessionApprove(params: SessionParamsVO.ApprovalParams, topic: TopicVO, requestId: Long): SequenceLifecycle {
        //TODO: Add isController check + Add params validation

        if (!sequenceStorageRepository.hasSessionTopic(topic)) {
            Logger.error("onSessionApprove: No pending session for topic: $topic")
            return EngineDO.FailedTopic
        }

        val pendingSession: SessionVO = sequenceStorageRepository.getSessionByTopic(topic)

        if (pendingSession.status != SequenceStatus.PROPOSED) {
            Logger.error("onSessionApprove: No pending session for topic: $topic")
            return EngineDO.NoSession
        }

        val (_, settledTopic) = crypto.generateTopicAndSharedKey(pendingSession.selfParticipant, PublicKey(params.responder.publicKey))
        val acknowledgedSession = pendingSession.toEngineDOSettledSessionVO(settledTopic, params)
        sequenceStorageRepository.updateProposedSessionToAcknowledged(acknowledgedSession, pendingSession.topic)

        relayer.subscribe(settledTopic)
        relayer.unsubscribe(pendingSession.topic)

        val jsonRpcResult = EngineDO.JsonRpcResponse.JsonRpcResult(id = requestId, result = "true")
        relayer.publishJsonRpcResponse(topic, jsonRpcResult.toJsonRpcResult(),
            onFailure = { error -> Logger.error("onSessionApproved: Cannot send the respond, error: $error") })
        return pendingSession.toSessionApproved(params.responder.metadata, settledTopic)
    }

    private fun onSessionReject(params: SessionParamsVO.RejectParams, topic: TopicVO): SequenceLifecycle {
        if (!sequenceStorageRepository.hasSessionTopic(topic)) {
            Logger.error("onSessionRejected: No session for topic: $topic")
            EngineDO.FailedTopic
        }

        sequenceStorageRepository.deleteSession(topic)
        relayer.unsubscribe(topic)
        return EngineDO.SessionRejected(topic.value, params.reason.message)
    }

    private fun onPairingPayload(payload: PairingParamsVO.PayloadParams, topic: TopicVO, requestId: Long): EngineDO.SessionProposal {
        //TODO: Add permission validation - if wc_sessionPropose

        if (!sequenceStorageRepository.hasPairingTopic(topic)) {
            Logger.error("onPairingPayload: No pairing for topic: $topic")
            EngineDO.FailedTopic
        }

        val proposal: SessionParamsVO.ProposalParams = payload.request.params
        val (sharedKey, publicKey) = crypto.getKeyAgreement(proposal.signal.params.topic)

        val proposedSession = proposal.toEngineDOSettledSessionVO(publicKey as PublicKey, controllerType)
        sequenceStorageRepository.insertSessionProposal(proposedSession, proposal.proposer.metadata, controllerType)
        crypto.setEncryptionKeys(sharedKey as SharedKey, publicKey, proposal.topic)

        val jsonRpcResult = EngineDO.JsonRpcResponse.JsonRpcResult(id = requestId, result = "true")
        relayer.publishJsonRpcResponse(topic, response = jsonRpcResult.toJsonRpcResult(),
            onFailure = { error -> Logger.error("onPairingPayload: Cannot send the respond, error: $error") })
        return payload.toEngineDOSessionProposal()
    }

    private fun onSessionPayload(params: SessionParamsVO.SessionPayloadParams, topic: TopicVO, requestId: Long): EngineDO.SessionRequest {
        //TODO: Add SessionPayload validation

        if (!sequenceStorageRepository.hasSessionTopic(topic)) {
            Logger.error("onSessionPayload: No session for topic: $topic")
            EngineDO.FailedTopic
        }
        return params.toEngineDOSessionRequest(topic, requestId)
    }

    private fun onSessionDelete(params: SessionParamsVO.DeleteParams, topic: TopicVO): EngineDO.DeletedSession {
        if (!sequenceStorageRepository.hasSessionTopic(topic)) {
            Logger.error("onSessionDelete: No session for topic: $topic")
            EngineDO.FailedTopic
        }

        crypto.removeKeys(topic.value)
        sequenceStorageRepository.deleteSession(topic)
        relayer.unsubscribe(topic)
        return params.toEngineDoDeleteSession(topic)
    }

    private fun onSessionNotification(params: SessionParamsVO.NotificationParams, topic: TopicVO): EngineDO.SessionNotification {
        //TODO: Add Notification validation

        if (!sequenceStorageRepository.hasSessionTopic(topic)) {
            Logger.error("onSessionNotification: No session for topic: $topic")
            EngineDO.FailedTopic
        }
        return params.toEngineDoSessionNotification(topic)
    }

    private fun onPairingDelete(params: PairingParamsVO.DeleteParams, topic: TopicVO): EngineDO.DeletedPairing {
        if (!sequenceStorageRepository.hasPairingTopic(topic)) {
            Logger.error("onPairingDelete: No pairing for topic: $topic")
            EngineDO.FailedTopic
        }

        crypto.removeKeys(topic.value)
        relayer.unsubscribe(topic)
        sequenceStorageRepository.deletePairing(topic)
        return EngineDO.DeletedPairing(topic.value, params.reason.message)
    }

    private fun onPing(topic: TopicVO, requestId: Long): EngineDO.Default {
        val jsonRpcResult = EngineDO.JsonRpcResponse.JsonRpcResult(id = requestId, result = "true")
        relayer.publishJsonRpcResponse(
            topic, jsonRpcResult.toJsonRpcResult(),
            { Logger.log("Ping send successfully") },
            { error -> Logger.error("Ping Error: $error") })
        return EngineDO.Default
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

    internal fun getListOfJsonRpcHistory(topic: String) {
        relayer.getJsonRpcHistory(topic)
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

    private fun checkPeer(currentPeer: ControllerType, message: String) {
        if (controllerType != currentPeer) {
            throw WalletConnectException.UnauthorizedPeerException(message)
        }
    }

    private fun ExpiryVO.isSequenceValid(): Boolean = seconds > (System.currentTimeMillis() / 1000)

    private fun generateTopic(): TopicVO = TopicVO(randomBytes(32).bytesToHex())
}