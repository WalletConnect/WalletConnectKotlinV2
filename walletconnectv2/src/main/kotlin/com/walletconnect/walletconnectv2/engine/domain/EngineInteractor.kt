package com.walletconnect.walletconnectv2.engine.domain

import android.app.Application
import com.walletconnect.walletconnectv2.common.model.type.ControllerType
import com.walletconnect.walletconnectv2.common.model.type.SequenceLifecycle
import com.walletconnect.walletconnectv2.common.model.vo.*
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.pairing.PairingParamsVO
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.pairing.after.PostSettlementPairingVO
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.pairing.before.success.PairingStateVO
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.session.SessionParamsVO
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.session.after.PostSettlementSessionVO
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.session.after.params.ReasonVO
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.session.before.PreSettlementSessionVO
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.session.before.proposal.RelayProtocolOptionsVO
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.session.before.proposal.SessionProposerVO
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.session.before.proposal.SessionSignalVO
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.session.before.success.SessionParticipantVO
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.session.common.SessionStateVO
import com.walletconnect.walletconnectv2.common.model.vo.sequence.PairingVO
import com.walletconnect.walletconnectv2.common.model.vo.sequence.SessionVO
import com.walletconnect.walletconnectv2.common.scope.scope
import com.walletconnect.walletconnectv2.crypto.CryptoRepository
import com.walletconnect.walletconnectv2.crypto.data.repository.BouncyCastleCryptoRepository
import com.walletconnect.walletconnectv2.engine.model.EngineDO
import com.walletconnect.walletconnectv2.engine.model.mapper.*
import com.walletconnect.walletconnectv2.relay.domain.WalletConnectRelayer
import com.walletconnect.walletconnectv2.storage.sequence.SequenceStatus
import com.walletconnect.walletconnectv2.storage.sequence.SequenceStorageRepository
import com.walletconnect.walletconnectv2.util.Logger
import com.walletconnect.walletconnectv2.util.generateId
import com.walletconnect.walletconnectv2.util.generateTopic
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.*

internal class EngineInteractor {
    //region provide with DI
    // TODO: add logic to check hostName for ws/wss scheme with and without ://
    private var relayer: WalletConnectRelayer = WalletConnectRelayer()
    private lateinit var storageRepository: SequenceStorageRepository
    private val crypto: CryptoRepository = BouncyCastleCryptoRepository()
    //endregion

    private var metaData: EngineDO.AppMetaData? = null
    private var controllerType = ControllerType.CONTROLLER
    private var isController: Boolean = false
    private lateinit var sessionPermissions: EngineDO.SessionPermissions

    val sequenceEvent: StateFlow<SequenceLifecycle> =
        relayer.clientSyncJsonRpc
            .map { payload -> handleClientSyncJsonRpc(payload) }
            .stateIn(scope, SharingStarted.Lazily, EngineDO.Default)

    internal fun initialize(engine: EngineFactory) = with(engine) {
        this@EngineInteractor.metaData = metaData
        this@EngineInteractor.controllerType = if (isController) ControllerType.CONTROLLER else ControllerType.NON_CONTROLLER
        this@EngineInteractor.isController = isController

        WalletConnectRelayer.RelayFactory(useTLs, hostName, projectId, application).run { relayer.initialize(this) }

        storageRepository = SequenceStorageRepository(null, application)

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
        return if (pairingTopic != null) {
            proposeSession(permissions, pairingTopic)
            null
        } else {
            proposePairing(permissions)
        }
    }

    private fun proposeSession(permissions: EngineDO.SessionPermissions, pairingTopic: String) {
        val settledPairing: PairingVO? = storageRepository.getPairingByTopic(TopicVO(pairingTopic))
        if (settledPairing != null && settledPairing.status == SequenceStatus.ACKNOWLEDGED) {
            val pendingSessionTopic = generateTopic()
            val selfPublicKey = crypto.generateKeyPair()

            val proposalParams = SessionParamsVO.ProposalParams(
                pendingSessionTopic,
                RelayProtocolOptionsVO(),
                SessionProposerVO(selfPublicKey.keyAsHex, isController, metaData?.toClientSyncMetaData()),
                SessionSignalVO(params = SessionSignalVO.Params(settledPairing.topic)),
                permissions.toSessionsProposedPermissions(),
                TtlVO(pendingSequenceExpirySeconds())
            )

            val proposedSession =
                SessionVO(
                    pendingSessionTopic,
                    ExpiryVO(pendingSequenceExpirySeconds()),
                    SequenceStatus.PROPOSED,
                    selfPublicKey,
                    chains = proposalParams.permissions.blockchain.chains,
                    methods = proposalParams.permissions.jsonRpc.methods,
                    types = proposalParams.permissions.notifications.types,
                    ttl = TtlVO(pendingSequenceExpirySeconds()),
                    controllerType = controllerType,
                    relayProtocol = proposalParams.relay.protocol
                )

            //INSERTING SESSION PENDING - PROPOSED
            storageRepository.insertSessionProposal(proposedSession, metaData?.toClientSyncMetaData(), controllerType)

            relayer.subscribe(pendingSessionTopic)

            val (sharedKey, publicKey) = crypto.getKeyAgreement(settledPairing.topic)
            crypto.setEncryptionKeys(sharedKey as SharedKey, publicKey as PublicKey, pendingSessionTopic)
            val sessionProposal = PreSettlementSessionVO.Proposal(id = generateId(), params = proposalParams)

            relayer.request(settledPairing.topic, sessionProposal) { result ->
                result.fold(
                    onSuccess = { Logger.log("Session proposal response received") },
                    onFailure = { error ->
                        Logger.error("Session proposal sent error: $error")
                        //TODO: Check
                        with(proposalParams) {
                            relayer.unsubscribe(topic)
                            storageRepository.deleteSession(topic)
                            crypto.removeKeys(topic.value)
                        }
                    }
                )
            }

        } else {
            //Error
        }
    }

    private fun proposePairing(permissions: EngineDO.SessionPermissions): String {
        val topic: TopicVO = generateTopic()
        val publicKey: PublicKey = crypto.generateKeyPair()
        val relay = RelayProtocolOptionsVO()
        val uri = EngineDO.WalletConnectUri(topic.value, publicKey.keyAsHex, isController, relay).toAbsoluteString()

        //PAIRING PENDING - PROPOSED
        val proposedPairing = PairingVO(
            topic,
            ExpiryVO(pendingSequenceExpirySeconds()),
            SequenceStatus.PROPOSED,
            publicKey,
            proposalUri = uri,
            relayProtocol = relay.protocol
        )
        storageRepository.insertPendingPairing(proposedPairing, controllerType)

        relayer.subscribe(topic)
        sessionPermissions = permissions
        return uri
    }

    internal fun pair(uri: String, onSuccess: (String) -> Unit, onFailure: (Throwable) -> Unit) {
        val proposal: PairingParamsVO.Proposal = uri.toPairProposal()
        val proposalTopic: TopicVO = proposal.pendingTopic
        val selfPublicKey: PublicKey = crypto.generateKeyPair()
        val settledExpiry = ExpiryVO((Calendar.getInstance().timeInMillis / 1000) + proposal.ttl.seconds)
        val pendingExpiry = ExpiryVO(pendingSequenceExpirySeconds())
        val peerPublicKey = PublicKey(proposal.pairingProposer.publicKey)
        val controllerKey = if (proposal.pairingProposer.controller) peerPublicKey else selfPublicKey
        val permissions: List<String>? = proposal.permissions?.jsonRPC?.methods

        val (_, settledTopic) = crypto.generateTopicAndSharedKey(selfPublicKey, peerPublicKey)

        val relay = proposal.relay
        //2x TODO: convert JSONObject to String to get the protocol type
        val respondedPairing =
            PairingVO(proposalTopic, pendingExpiry, SequenceStatus.RESPONDED, selfPublicKey, proposalUri = uri, relayProtocol = "relay")
        val preSettledPairing = PairingVO(
            settledTopic,
            settledExpiry,
            SequenceStatus.PRE_SETTLED,
            selfPublicKey,
            peerPublicKey,
            relayProtocol = "relay",
            controllerKey = controllerKey,
            proposalUri = uri,
            permissions = permissions
        )

        relayer.subscribe(proposal.pendingTopic)
        storageRepository.insertPendingPairing(respondedPairing, controllerType)

        relayer.subscribe(settledTopic)
        storageRepository.updateRespondedPairingToPreSettled(proposalTopic, preSettledPairing)

        val preSettlementPairingApprove = proposal.toApprove(generateId(), settledTopic, settledExpiry, selfPublicKey)
        relayer.isConnectionOpened
            .filter { isOnline -> isOnline }
            .onEach {
                supervisorScope {
                    relayer.request(proposalTopic, preSettlementPairingApprove) { result ->
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
        storageRepository.updatePreSettledPairingToAcknowledged(sequence.copy(status = SequenceStatus.ACKNOWLEDGED))
        relayer.unsubscribe(proposal.pendingTopic)
        onSuccess(sequence.topic.value)
        pairingUpdate(sequence)
    }

    private fun pairingUpdate(settledSequence: PairingVO) {
        val pairingUpdate: PostSettlementPairingVO.PairingUpdate =
            PostSettlementPairingVO.PairingUpdate(
                id = generateId(),
                params = PairingParamsVO.UpdateParams(state = PairingStateVO(metaData?.toClientSyncMetaData()))
            )
        relayer.request(settledSequence.topic, pairingUpdate) { result ->
            result.fold(
                onSuccess = { Logger.log("Pairing update success") },
                onFailure = { error -> Logger.error("Pairing update error: $error") }
            )
        }
    }

    internal fun approve(proposal: EngineDO.SessionProposal, onSuccess: (EngineDO.SettledSession) -> Unit, onFailure: (Throwable) -> Unit) {
        val selfPublicKey: PublicKey = crypto.generateKeyPair()
        val peerPublicKey = PublicKey(proposal.proposerPublicKey)
        val sessionState = SessionStateVO(proposal.accounts)
        val expiryVO = ExpiryVO((Calendar.getInstance().timeInMillis / 1000) + proposal.ttl)
        val (_, settledTopic) = crypto.generateTopicAndSharedKey(selfPublicKey, peerPublicKey)
        val pendingTopic = TopicVO(proposal.topic)
        val sessionApprove = PreSettlementSessionVO.Approve(
            id = generateId(), params = SessionParamsVO.ApprovalParams(
                relay = RelayProtocolOptionsVO(), state = sessionState, expiry = expiryVO,
                responder = SessionParticipantVO(selfPublicKey.keyAsHex, metadata = metaData?.toClientSyncMetaData())
            )
        )
        val controllerKey = if (proposal.isController) peerPublicKey else selfPublicKey

        //SESSION: PENDING - RESPONDED; SETTLED - PRE_SETTLED

        val respondedSession = SessionVO(
            pendingTopic,
            ExpiryVO(pendingSequenceExpirySeconds()),
            SequenceStatus.RESPONDED,
            selfPublicKey,
            chains = proposal.chains,
            methods = proposal.methods,
            types = proposal.types,
            ttl = TtlVO(proposal.ttl),
            controllerType = controllerType,
            relayProtocol = proposal.relayProtocol
        )

        val preSettledSession = SessionVO(
            settledTopic,
            expiryVO,
            SequenceStatus.PRE_SETTLED,
            selfPublicKey,
            peerPublicKey,
            controllerKey,
            proposal.chains,
            proposal.methods,
            proposal.types,
            TtlVO(proposal.ttl),
            proposal.accounts,
            controllerType = controllerType,
            relayProtocol = proposal.relayProtocol
        )


        storageRepository.updateProposedSessionToResponded(respondedSession)
        relayer.subscribe(TopicVO(proposal.topic))

        relayer.subscribe(settledTopic)
        storageRepository.updateRespondedSessionToPreSettled(preSettledSession, pendingTopic)

        relayer.request(TopicVO(proposal.topic), sessionApprove) { result ->
            result.fold(
                onSuccess = {
                    relayer.unsubscribe(TopicVO(proposal.topic))
                    crypto.removeKeys(proposal.topic)
                    storageRepository.updatePreSettledSessionToAcknowledged(preSettledSession.copy(status = SequenceStatus.ACKNOWLEDGED))
                    onSuccess(proposal.toAcknowledgedSession(settledTopic, expiryVO))
                },
                onFailure = { error ->
                    relayer.unsubscribe(TopicVO(proposal.topic))
                    relayer.unsubscribe(settledTopic)
                    crypto.removeKeys(proposal.topic)
                    crypto.removeKeys(settledTopic.value)
                    storageRepository.deleteSession(settledTopic)
                    onFailure(error)
                }
            )
        }
    }

    internal fun reject(reason: String, topic: String, onSuccess: (Pair<String, String>) -> Unit, onFailure: (Throwable) -> Unit) {
        val sessionReject = PreSettlementSessionVO.Reject(id = generateId(), params = SessionParamsVO.RejectParams(reason = reason))
        onSuccess(Pair(topic, reason))
        storageRepository.deleteSession(TopicVO(topic))
        relayer.request(TopicVO(topic), sessionReject) { result ->
            result.fold(
                onSuccess = {}, //TODO: Should we unsubscribe from topic?
                onFailure = { error -> onFailure(error) }
            )
        }
    }

    internal fun disconnect(topic: String, reason: String, onSuccess: (Pair<String, String>) -> Unit, onFailure: (Throwable) -> Unit) {
        val sessionDelete =
            PostSettlementSessionVO.SessionDelete(id = generateId(), params = SessionParamsVO.DeleteParams(ReasonVO(message = reason)))
        storageRepository.deleteSession(TopicVO(topic))
        relayer.unsubscribe(TopicVO(topic))
        onSuccess(Pair(topic, reason))
        relayer.request(TopicVO(topic), sessionDelete) { result ->
            result.fold(
                onSuccess = {/*TODO: Should wait for acknowledgement and delete keys?*/ },
                onFailure = { error -> onFailure(error) }
            )
        }
    }

    internal fun respondSessionPayload(topic: String, jsonRpcResponse: JsonRpcResponseVO, onFailure: (Throwable) -> Unit) {
        relayer.respond(TopicVO(topic), jsonRpcResponse,
            { Logger.error("Session payload sent successfully") },
            { error ->
                onFailure(error)
                Logger.error("Sending session payload error: $error")
            })
    }

    internal fun update(
        topic: String,
        sessionState: EngineDO.SessionState,
        onSuccess: (Pair<String, List<String>>) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        val sessionUpdate: PostSettlementSessionVO.SessionUpdate =
            PostSettlementSessionVO.SessionUpdate(
                id = generateId(),
                params = SessionParamsVO.UpdateParams(SessionStateVO(sessionState.accounts))
            )
        storageRepository.updateSessionWithAccounts(topic, sessionState.accounts)
        relayer.request(TopicVO(topic), sessionUpdate) { result ->
            result.fold(
                onSuccess = { onSuccess(Pair(topic, sessionState.accounts)) },
                onFailure = { error -> onFailure(error) }
            )
        }
    }

    internal fun upgrade(
        topic: String, permissions: EngineDO.SessionPermissions,
        onSuccess: (Pair<String, EngineDO.SessionPermissions>) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        val sessionUpgrade = PostSettlementSessionVO.SessionUpgrade(
            id = generateId(),
            params = SessionParamsVO.SessionPermissionsParams(permissions = permissions.toSessionsPermissions())
        )
        storageRepository.updateSessionWithPermissions(topic, permissions.blockchain?.chains, permissions.jsonRpc?.methods)
        relayer.request(TopicVO(topic), sessionUpgrade) { result ->
            result.fold(
                onSuccess = { onSuccess(Pair(topic, permissions)) },
                onFailure = { error -> onFailure(error) }
            )
        }
    }

    internal fun notify(topic: String, notification: EngineDO.Notification, onSuccess: (String) -> Unit, onFailure: (Throwable) -> Unit) {
        /*TODO check whether under given topic there is a pairing or session stored and create proper Notification class*/
        //val pairingNotification = PostSettlementPairing.PairingNotification(id = generateId(), params = Pairing.NotificationParams(notification.type, notification.data))
        val sessionNotification =
            PostSettlementSessionVO
                .SessionNotification(id = generateId(), params = SessionParamsVO.NotificationParams(notification.type, notification.data))
        relayer.request(TopicVO(topic), sessionNotification) { result ->
            result.fold(
                onSuccess = { onSuccess(topic) },
                onFailure = { error -> onFailure(error) }
            )
        }
    }

    internal fun ping(topic: String, onSuccess: (String) -> Unit, onFailure: (Throwable) -> Unit) {
        /*TODO check whether under given topic there is a pairing or session stored and create proper Ping class*/
        //val pairingParams = PostSettlementPairing.PairingPing(id = generateId(), params = Pairing.PingParams())
        val sessionPing = PostSettlementSessionVO.SessionPing(id = generateId(), params = SessionParamsVO.PingParams())
        relayer.request(TopicVO(topic), sessionPing) { result ->
            result.fold(
                onSuccess = { onSuccess(topic) },
                onFailure = { error -> onFailure(error) }
            )
        }
    }

    private fun handleClientSyncJsonRpc(payload: RequestSubscriptionPayloadVO) =
        when (payload.params) {
            is PairingParamsVO.ApproveParams -> onPairingApproved(payload.params, payload.topic, payload.requestId)
            is SessionParamsVO.ApprovalParams -> onSessionApproved(payload.params, payload.topic, payload.requestId)
            is SessionParamsVO.RejectParams -> onSessionRejected(payload.params, payload.topic)
            is PairingParamsVO.PayloadParams -> onPairingPayload(payload.params)
            is SessionParamsVO.DeleteParams -> onSessionDelete(payload.params, payload.topic)
            is SessionParamsVO.SessionPayloadParams -> onSessionPayload(payload.params, payload.topic, payload.requestId)
            is SessionParamsVO.NotificationParams -> onSessionNotification(payload.params, payload.topic)
            is PairingParamsVO.DeleteParams -> onPairingDelete(payload.params, payload.topic)
            is PairingParamsVO.PingParams, is SessionParamsVO.PingParams -> onPing(payload.topic, payload.requestId)
            else -> EngineDO.Default
        }

    private fun onSessionApproved(params: SessionParamsVO.ApprovalParams, topic: TopicVO, requestId: Long): SequenceLifecycle {
        if (!isController) {
            Logger.log("onSessionApproved: Unexpected method call by non-controller client")
            return EngineDO.Default
        }

        val pendingSession = storageRepository.getSessionByTopic(topic)

        if (pendingSession == null || pendingSession.status != SequenceStatus.PROPOSED) {
            Logger.log("onSessionApproved: No pending session for topic: $topic")
            return EngineDO.Default
        }

        val peerPublicKey = PublicKey(params.responder.publicKey)
        val (_, settledTopic) = crypto.generateTopicAndSharedKey(pendingSession.selfParticipant, peerPublicKey)
        val controllerKey = if (pendingSession.controllerType == ControllerType.CONTROLLER) pendingSession.selfParticipant else peerPublicKey

        val acknowledgedSession = SessionVO(
            settledTopic,
            params.expiry,
            SequenceStatus.ACKNOWLEDGED,
            pendingSession.selfParticipant,
            peerPublicKey,
            controllerKey,
            controllerType = pendingSession.controllerType,
            appMetaData = params.responder.metadata,
            relayProtocol = params.relay.protocol,
            chains = pendingSession.chains,
            methods = pendingSession.methods,
            types = pendingSession.types,
            accounts = params.state.accounts,
            ttl = TtlVO(params.expiry.seconds)
        )
        storageRepository.updateProposedSessionToAcknowledged(acknowledgedSession, pendingSession.topic)
        relayer.subscribe(settledTopic)

        val jsonRpcResult = EngineDO.JsonRpcResponse.JsonRpcResult(id = requestId, result = "true")
        relayer.respond(pendingSession.topic, jsonRpcResult.toJsonRpcResponseVO(),
            {
                Logger.log("onSessionApproved: Respond Success")
//                relayer.unsubscribe(pendingSession.topic)
            },
            { error ->
                Logger.error("onSessionApproved: Respond Error: $error")
//                relayer.unsubscribe(pendingSession.topic)
            })

        return pendingSession.toSessionApproved(params.responder.metadata)
    }

    private fun onSessionRejected(params: SessionParamsVO.RejectParams, topic: TopicVO): SequenceLifecycle {
        storageRepository.getSessionByTopic(topic) ?: run {
            Logger.log("onSessionRejected: No session for topic: $topic")
            return EngineDO.Default
        }
        storageRepository.deleteSession(topic)
        relayer.unsubscribe(topic)
        return EngineDO.SessionRejected(topic.value, params.reason)
    }

    private fun onPairingApproved(params: PairingParamsVO.ApproveParams, pendingTopic: TopicVO, requestId: Long): SequenceLifecycle {
        val pendingPairing: PairingVO? = storageRepository.getPairingByTopic(pendingTopic)

        if (pendingPairing == null || pendingPairing.status != SequenceStatus.PROPOSED) {
            Logger.log("onPairingApproved: No pending pairing for topic: $pendingTopic")
            return EngineDO.Default
        }

        val peerPublicKey = PublicKey(params.responder.publicKey)
        val (_, settledTopic) = crypto.generateTopicAndSharedKey(pendingPairing.selfParticipant, peerPublicKey)
        val controllerKey: PublicKey = if (isController) pendingPairing.selfParticipant else peerPublicKey

        //TODO: move to mapper
        val acknowledgedPairing = PairingVO(
            settledTopic,
            params.expiry,
            SequenceStatus.ACKNOWLEDGED,
            pendingPairing.selfParticipant,
            peerPublicKey,
            controllerKey,
            pendingPairing.proposalUri,
            permissions = pendingPairing.permissions,
            relayProtocol = pendingPairing.relayProtocol
        )

        storageRepository.updateProposedPairingToAcknowledged(acknowledgedPairing, pendingTopic)
        relayer.subscribe(settledTopic)

        val jsonRpcResult = EngineDO.JsonRpcResponse.JsonRpcResult(id = requestId, result = "true")
        relayer.respond(pendingPairing.topic, jsonRpcResult.toJsonRpcResponseVO(),
            {
                Logger.log("onPairingApproved: Respond Success")
                relayer.unsubscribe(pendingPairing.topic)
            },
            { error ->
                Logger.error("onPairingApproved: Respond Error: $error")
                relayer.unsubscribe(pendingPairing.topic)
            })

        return acknowledgedPairing.toEngineDOSettledPairing(sessionPermissions)
    }

    private fun onPairingPayload(payload: PairingParamsVO.PayloadParams): EngineDO.SessionProposal {
        val proposal = payload.request.params
        val metadata = proposal.proposer.metadata
        val (sharedKey, publicKey) = crypto.getKeyAgreement(proposal.signal.params.topic)


        //TODO: add checks

        val proposedSession =
            SessionVO(
                proposal.topic,
                ExpiryVO(pendingSequenceExpirySeconds()),
                SequenceStatus.PROPOSED,
                publicKey as PublicKey,
                chains = proposal.permissions.blockchain.chains,
                methods = proposal.permissions.jsonRpc.methods,
                types = proposal.permissions.notifications.types,
                ttl = TtlVO(pendingSequenceExpirySeconds()),
                controllerType = controllerType,
                relayProtocol = payload.request.params.relay.protocol
            )

        storageRepository.insertSessionProposal(proposedSession, metadata, controllerType)
        crypto.setEncryptionKeys(sharedKey as SharedKey, publicKey, proposal.topic)
        //TODO add respond true
        return payload.toEngineDOSessionProposal()
    }

    private fun onSessionPayload(params: SessionParamsVO.SessionPayloadParams, topic: TopicVO, requestId: Long): EngineDO.SessionRequest {
        //TODO: validate session request against the permissions set
        return params.toEngineDOSessionRequest(topic, requestId)
    }

    private fun onSessionDelete(params: SessionParamsVO.DeleteParams, topic: TopicVO): EngineDO.DeletedSession {
        crypto.removeKeys(topic.value)
        storageRepository.deleteSession(topic)
        relayer.unsubscribe(topic)
        return params.toEngineDoDeleteSession(topic)
    }

    private fun onSessionNotification(params: SessionParamsVO.NotificationParams, topic: TopicVO): EngineDO.SessionNotification {
        //TODO: validate notification
        return params.toEngineDoSessionNotification(topic)
    }

    private fun onPairingDelete(params: PairingParamsVO.DeleteParams, topic: TopicVO): EngineDO.Default {
        crypto.removeKeys(topic.value)
        relayer.unsubscribe(topic)
        //TODO delete from DB
        return EngineDO.Default
    }

    private fun onPing(topic: TopicVO, requestId: Long): EngineDO.Default {
        val jsonRpcResult = EngineDO.JsonRpcResponse.JsonRpcResult(id = requestId, result = "true")
        relayer.respond(topic, jsonRpcResult.toJsonRpcResponseVO(),
            { Logger.log("Ping send successfully") },
            { error -> Logger.error("Ping Error: $error") })
        return EngineDO.Default
    }

    internal fun getListOfPendingSessions(): List<EngineDO.SessionProposal> {
        return storageRepository.getListOfSessionVOs()
            .filter { session -> session.status == SequenceStatus.PROPOSED && session.expiry.isSequenceValid() }
            .map { session -> session.toEngineDOSessionProposal(crypto.getKeyAgreement(session.topic).second as PublicKey) }
    }

    internal fun getListOfSettledSessions(): List<EngineDO.SettledSession> =
        storageRepository.getListOfSessionVOs()
            .filter { session -> session.status == SequenceStatus.ACKNOWLEDGED && session.expiry.isSequenceValid() }
            .map { session -> session.toEngineDOSettledSession() }

    private fun resubscribeToSettledPairings() {
        val (listOfExpiredPairing, listOfValidPairing) = storageRepository.getListOfPairingVOs()
            .partition { pairing -> !pairing.expiry.isSequenceValid() }

        listOfExpiredPairing
            .map { pairing -> pairing.topic }
            .onEach { pairingTopic ->
                relayer.unsubscribe(pairingTopic)
                storageRepository.deletePairing(pairingTopic)
            }

        listOfValidPairing
            .filter { pairing -> pairing.status == SequenceStatus.ACKNOWLEDGED }
            .map { pairing -> pairing.topic }
            .onEach { pairingTopic -> relayer.subscribe(pairingTopic) }
    }

    private fun resubscribeToSettledSession() {
        val (listOfExpiredSession, listOfValidSessions) = storageRepository.getListOfSessionVOs()
            .partition { session -> !session.expiry.isSequenceValid() }

        listOfExpiredSession
            .map { session -> session.topic }
            .onEach { sessionTopic ->
                relayer.unsubscribe(sessionTopic)
                storageRepository.deleteSession(sessionTopic)
            }

        listOfValidSessions
            .filter { session -> session.status == SequenceStatus.ACKNOWLEDGED }
            .onEach { session -> relayer.subscribe(session.topic) }
    }

    private fun ExpiryVO.isSequenceValid(): Boolean = seconds > (System.currentTimeMillis() / 1000)
    private fun pendingSequenceExpirySeconds() = ((System.currentTimeMillis() / 1000) + 86400) //24h

    class EngineFactory(
        val useTLs: Boolean = false,
        val hostName: String,
        val projectId: String,
        val isController: Boolean,
        val application: Application,
        val metaData: EngineDO.AppMetaData
    )
}