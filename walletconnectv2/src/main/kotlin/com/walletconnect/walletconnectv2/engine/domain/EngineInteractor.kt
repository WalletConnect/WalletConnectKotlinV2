package com.walletconnect.walletconnectv2.engine.domain

import android.app.Application
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.pairing.PairingParamsVO
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.pairing.after.PostSettlementPairingVO
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.pairing.before.success.PairingParticipantVO
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.pairing.before.success.PairingStateVO
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.session.SessionParamsVO
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.session.after.PostSettlementSessionVO
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.session.after.params.ReasonVO
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.session.before.PreSettlementSessionVO
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.session.before.proposal.RelayProtocolOptionsVO
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.session.before.success.SessionParticipantVO
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.session.common.SessionStateVO
import com.walletconnect.walletconnectv2.common.model.type.ControllerType
import com.walletconnect.walletconnectv2.common.model.type.SequenceLifecycle
import com.walletconnect.walletconnectv2.common.model.vo.*
import com.walletconnect.walletconnectv2.crypto.CryptoRepository
import com.walletconnect.walletconnectv2.crypto.data.repository.BouncyCastleCryptoRepository
import com.walletconnect.walletconnectv2.engine.model.EngineDO
import com.walletconnect.walletconnectv2.relay.domain.WalletConnectRelayer
import com.walletconnect.walletconnectv2.common.scope.scope
import com.walletconnect.walletconnectv2.engine.model.mapper.*
import com.walletconnect.walletconnectv2.engine.model.mapper.toApprove
import com.walletconnect.walletconnectv2.engine.model.mapper.toPairProposal
import com.walletconnect.walletconnectv2.engine.model.mapper.toClientSyncAppMetaData
import com.walletconnect.walletconnectv2.engine.model.mapper.toSessionProposal
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.pairing.before.proposal.*
import com.walletconnect.walletconnectv2.storage.sequence.SequenceStatus
import com.walletconnect.walletconnectv2.storage.sequence.SequenceStorageRepository
import com.walletconnect.walletconnectv2.util.Empty
import com.walletconnect.walletconnectv2.util.Logger
import com.walletconnect.walletconnectv2.util.generateId
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.json.JSONObject
import java.net.URI
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
//    private val _sequenceEvent: MutableStateFlow<SequenceLifecycle> = MutableStateFlow(SequenceLifecycle.Default)
//    private val _sequenceEvent = relayer.clientSyncJsonRpc.map {
//        //map fro VO to EngineDO
//    }.stateIn(scope, SharingStarted.Lazily, SequenceLifecycle.Default)

    //TODO: samo to wystarczy
    val sequenceEvent: StateFlow<SequenceLifecycle> = relayer.clientSyncJsonRpc.map {
        //map fro VO to EngineDO
    }.stateIn(scope, SharingStarted.Lazily, SequenceLifecycle.Default)

    internal fun initialize(engine: EngineFactory) = with(engine) {
        this@EngineInteractor.metaData = engine.metaData
        this@EngineInteractor.controllerType = if (engine.isController) ControllerType.CONTROLLER else ControllerType.NON_CONTROLLER
        WalletConnectRelayer.RelayFactory(useTLs, hostName, projectId, application).run {
            relayer.initialize(this)
        }
        storageRepository = SequenceStorageRepository(null, engine.application)
        collectClientSyncJsonRpc()

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

    internal fun pair(uri: String, onSuccess: (String) -> Unit, onFailure: (Throwable) -> Unit) {
        val pairingProposal: PairingParamsVO.Proposal = uri.toPairProposal()
        storageRepository.insertPairingProposal(
            pairingProposal.topic.value,
            uri,
            defaultSequenceExpirySeconds(),
            SequenceStatus.PENDING,
            controllerType
        )
        relayer.subscribe(pairingProposal.topic)
        val selfPublicKey: PublicKey = crypto.generateKeyPair()
        val expiry = ExpiryVO((Calendar.getInstance().timeInMillis / 1000) + pairingProposal.ttl.seconds)
        val peerPublicKey = PublicKey(pairingProposal.pairingProposer.publicKey)
        val controllerPublicKey = if (pairingProposal.pairingProposer.controller) {
            peerPublicKey
        } else {
            selfPublicKey
        }
        val settledSequence = settlePairingSequence(pairingProposal.relay, selfPublicKey, peerPublicKey, controllerPublicKey, expiry)
        val preSettlementPairingApprove = pairingProposal.toApprove(generateId(), settledSequence.topic, expiry, selfPublicKey)

        relayer.isConnectionOpened
            .filter { isOnline -> isOnline }
            .onEach {
                supervisorScope {
                    relayer.request(pairingProposal.topic, preSettlementPairingApprove) { result ->
                        result.fold(
                            onSuccess = {
                                relayer.unsubscribe(pairingProposal.topic)
                                relayer.subscribe(settledSequence.topic)
                                storageRepository.updatePendingPairingToSettled(
                                    pairingProposal.topic.value,
                                    settledSequence.topic.value,
                                    expiry.seconds,
                                    SequenceStatus.SETTLED
                                )
                                onSuccess(settledSequence.topic.value)
                                pairingUpdate(settledSequence)
                            },
                            onFailure = { throwable -> onFailure(throwable) }
                        )
                    }
                    cancel()
                }
            }.launchIn(scope)
    }

    private fun pairingUpdate(settledSequence: EngineDO.SettledPairing) {
        val pairingUpdate: PostSettlementPairingVO.PairingUpdate =
            PostSettlementPairingVO.PairingUpdate(
                id = generateId(),
                params = PairingParamsVO.UpdateParams(state = PairingStateVO(metaData?.toClientSyncAppMetaData()))
            )
        relayer.request(settledSequence.topic, pairingUpdate) { result ->
            result.fold(
                onSuccess = {
                    /*TODO update Pairing's metadata in local storage
                    *  Might not need to store pairing metadata because metadata is a global variable*/
                },
                onFailure = { error -> Logger.error("Pairing update error: $error") }
            )
        }
    }

    internal fun approve(
        proposal: EngineDO.SessionProposal,
        onSuccess: (EngineDO.SettledSession) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        val selfPublicKey: PublicKey = crypto.generateKeyPair()
        val peerPublicKey = PublicKey(proposal.proposerPublicKey)
        val sessionState = SessionStateVO(proposal.accounts)
        val expiry = ExpiryVO((Calendar.getInstance().timeInMillis / 1000) + proposal.ttl)
        val (_, settledTopic) = crypto.generateTopicAndSharedKey(selfPublicKey, peerPublicKey)
        relayer.subscribe(TopicVO(proposal.topic))

        val sessionApprove = PreSettlementSessionVO.Approve(
            id = generateId(), params = SessionParamsVO.Success(
                relay = RelayProtocolOptionsVO(), state = sessionState, expiry = expiry,
                responder = SessionParticipantVO(selfPublicKey.keyAsHex, metadata = metaData?.toClientSyncAppMetaData())
            )
        )

        relayer.request(TopicVO(proposal.topic), sessionApprove) { result ->
            result.fold(
                onSuccess = {
                    relayer.unsubscribe(TopicVO(proposal.topic))
                    relayer.subscribe(settledTopic)

                    with(proposal) {
                        storageRepository.updateStatusToSessionApproval(
                            topic,
                            sessionApprove.id,
                            settledTopic.value,
                            sessionApprove.params.state.accounts,
                            sessionApprove.params.expiry.seconds
                        )

                        val engineDataSettledSession = EngineDO.SettledSession(
                            settledTopic,
                            accounts,
                            EngineDO.AppMetaData(name, description, url, icons.map { iconUri -> iconUri.toString() }),
                            EngineDO.SettledSession.Permissions(
                                EngineDO.SettledSession.Permissions.Blockchain(chains),
                                EngineDO.SettledSession.Permissions.JsonRpc(methods),
                                EngineDO.SettledSession.Permissions.Notifications(types)
                            ),
                            expiry,
                            SequenceStatus.SETTLED
                        )
                        onSuccess(engineDataSettledSession)
                    }
                },
                onFailure = { error -> onFailure(error) }
            )
        }
    }

    internal fun reject(reason: String, topic: String, onSuccess: (Pair<String, String>) -> Unit, onFailure: (Throwable) -> Unit) {
        val sessionReject = PreSettlementSessionVO.Reject(id = generateId(), params = SessionParamsVO.Failure(reason = reason))

        onSuccess(Pair(topic, reason))
        storageRepository.deleteSession(topic)
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
        storageRepository.deleteSession(topic)
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
        relayer.respond(
            TopicVO(topic), jsonRpcResponse, { Logger.error("Session payload sent successfully") },
            { error ->
                onFailure(error)
                Logger.error("Sending session payload error: $error")
            })
    }

    internal fun update(
        topic: String, sessionState: EngineDO.SessionState,
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

    internal fun notify(
        topic: String, notification: EngineDO.Notification,
        onSuccess: (String) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
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

    internal fun getListOfPendingSessions(): List<EngineDO.SessionProposal> {
        return storageRepository.getListOfSessionVOs().filter { session ->
            session.status == SequenceStatus.PENDING && session.expiry.isSequenceValid()
        }.map { session ->
            val (_, peerPublicKey) = crypto.getKeyAgreement(session.topic)

            EngineDO.SessionProposal(
                name = session.appMetaData?.name ?: String.Empty,
                description = session.appMetaData?.description ?: String.Empty,
                url = session.appMetaData?.url ?: String.Empty,
                icons = session.appMetaData?.icons?.map { URI(it) } ?: emptyList(),
                chains = session.chains,
                methods = session.methods,
                types = session.types,
                topic = session.topic.value,
                proposerPublicKey = peerPublicKey.keyAsHex,
                ttl = session.ttl.seconds,
                accounts = session.accounts
            )
        }
    }

    internal fun getListOfSettledSessions(): List<EngineDO.SettledSession> =
        storageRepository.getListOfSessionVOs()
            .filter { session -> session.status == SequenceStatus.SETTLED && session.expiry.isSequenceValid() }
            .map { session ->

                val metadata: EngineDO.AppMetaData? =
                    session.appMetaData?.let { metaData ->
                        EngineDO.AppMetaData(metaData.name, metaData.description, metaData.url, metaData.icons)
                    }

                EngineDO.SettledSession(
                    session.topic,
                    session.accounts,
                    metadata,
                    EngineDO.SettledSession.Permissions(
                        EngineDO.SettledSession.Permissions.Blockchain(session.chains),
                        EngineDO.SettledSession.Permissions.JsonRpc(session.methods),
                        EngineDO.SettledSession.Permissions.Notifications(session.types)
                    ),
                    session.expiry,
                    session.status
                )
            }

    //TODO: move to Relayer, decrypt in the Relayer, RelayDTO which maps SessionProposalVO, then map to EngineDO
    private fun collectClientSyncJsonRpc() = scope.launch {

        relayer.clientSyncJsonRpc.collect { payload ->
            when (payload.params) {
                is PairingParamsVO.PayloadParams -> onPairingPayload(payload.params)
                is SessionParamsVO.DeleteParams -> onSessionDelete(payload.params, payload.topic)
                is SessionParamsVO.SessionPayloadParams -> onSessionPayload(payload.params, payload.topic, payload.requestId)
                is PairingParamsVO.DeleteParams -> onPairingDelete(payload.params, payload.topic)
                is SessionParamsVO.NotificationParams -> onSessionNotification(payload.params, payload.topic)
                is PairingParamsVO.PingParams, is SessionParamsVO.PingParams -> onPing(payload.topic, payload.requestId)
            }
        }
    }

    private fun onPing(topic: TopicVO, requestId: Long) {
        val jsonRpcResult = EngineDO.JsonRpcResponse.JsonRpcResult(id = requestId, result = "true")
        relayer.respond(topic, jsonRpcResult.toJsonRpcResponseVO(),
            { Logger.log("Ping send successfully") },
            { error -> Logger.error("Ping Error: $error") })
    }

    private fun onPairingPayload(payload: PairingParamsVO.PayloadParams) {
        val proposal = payload.request.params
        storageRepository.insertSessionProposal(proposal, proposal.proposer.metadata, defaultSequenceExpirySeconds(), controllerType)

        val (sharedKey, publicKey) = crypto.getKeyAgreement(proposal.signal.params.topic)
        crypto.setEncryptionKeys(sharedKey as SharedKey, publicKey as PublicKey, proposal.topic)
        _sequenceEvent.value = EngineDO.SessionProposal(proposal.toSessionProposal())
    }

    private fun onSessionPayload(payload: SessionParamsVO.SessionPayloadParams, topic: TopicVO, requestId: Long) {
        //TODO Validate session request + add unmarshaling of generic session request payload to the usable generic object
        val params = payload.request.params.toString()
        val chainId = payload.chainId
        val method = payload.request.method
        _sequenceEvent.value = SequenceLifecycle.OnSessionRequest(
            EngineDO.SessionRequest(topic.value, chainId, EngineDO.SessionRequest.JSONRPCRequest(requestId, method, params))
        )
    }

    private fun onSessionDelete(params: SessionParamsVO.DeleteParams, topic: TopicVO) {
        crypto.removeKeys(topic.value)
        storageRepository.deleteSession(topic.value)
        relayer.unsubscribe(topic)
        _sequenceEvent.value = SequenceLifecycle.OnSessionDeleted(EngineDO.DeletedSession(topic.value, params.reason.message))
    }

    private fun onSessionNotification(params: SessionParamsVO.NotificationParams, topic: TopicVO) {
        val type = params.type
        val data = params.data.toString()
        _sequenceEvent.value = SequenceLifecycle.OnSessionNotification(EngineDO.SessionNotification(topic.value, type, data))
    }

    private fun onPairingDelete(params: PairingParamsVO.DeleteParams, topic: TopicVO) {
        crypto.removeKeys(topic.value)
        relayer.unsubscribe(topic)
        //TODO delete from DB
    }

    private fun resubscribeToSettledPairings() {
        val (listOfExpiredPairing, listOfValidPairing) = storageRepository.getListOfPairingVOs()
            .partition { pairing -> !pairing.expiry.isSequenceValid() }

        listOfExpiredPairing
            .map { pairing -> pairing.topic }
            .onEach { pairingTopic ->
                relayer.unsubscribe(pairingTopic)
                storageRepository.deletePairing(pairingTopic.value)
            }

        listOfValidPairing
            .filter { pairing -> pairing.status == SequenceStatus.SETTLED }
            .map { pairing -> pairing.topic }
            .onEach { pairingTopic ->
                relayer.subscribe(pairingTopic)
            }
    }

    private fun resubscribeToSettledSession() {
        val (listOfExpiredSession, listOfValidSessions) = storageRepository.getListOfSessionVOs()
            .partition { session -> !session.expiry.isSequenceValid() }

        listOfExpiredSession
            .map { session -> session.topic }
            .onEach { sessionTopic ->
                relayer.unsubscribe(sessionTopic)
                storageRepository.deleteSession(sessionTopic.value)
            }

        listOfValidSessions
            .filter { session -> session.status == SequenceStatus.SETTLED }
            .onEach { session ->
                relayer.subscribe(session.topic)
            }
    }

    private fun settlePairingSequence(
        relay: JSONObject,
        selfPublicKey: PublicKey,
        peerPublicKey: PublicKey,
        controllerPublicKey: PublicKey,
        expiry: ExpiryVO
    ): EngineDO.SettledPairing {
        val (_, settledTopic) = crypto.generateTopicAndSharedKey(selfPublicKey, peerPublicKey)
        return EngineDO.SettledPairing(
            settledTopic,
            relay,
            selfPublicKey,
            peerPublicKey,
            PairingPermissionsVO(PairingParticipantVO(controllerPublicKey.keyAsHex)),
            expiry,
            SequenceStatus.SETTLED
        )
    }

    private fun ExpiryVO.isSequenceValid(): Boolean = seconds > (System.currentTimeMillis() / 1000)
    private fun defaultSequenceExpirySeconds() = ((System.currentTimeMillis() / 1000) + 86400)

    class EngineFactory(
        val useTLs: Boolean = false,
        val hostName: String,
        val projectId: String,
        val isController: Boolean,
        val application: Application,
        val metaData: EngineDO.AppMetaData
    )
}