package org.walletconnect.walletconnectv2.engine

import android.app.Application
import com.tinder.scarlet.WebSocket
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import org.json.JSONObject
import org.walletconnect.walletconnectv2.clientsync.pairing.Pairing
import org.walletconnect.walletconnectv2.clientsync.pairing.SettledPairingSequence
import org.walletconnect.walletconnectv2.clientsync.pairing.after.PostSettlementPairing
import org.walletconnect.walletconnectv2.clientsync.pairing.before.proposal.PairingPermissions
import org.walletconnect.walletconnectv2.clientsync.pairing.before.success.PairingParticipant
import org.walletconnect.walletconnectv2.clientsync.pairing.before.success.PairingState
import org.walletconnect.walletconnectv2.clientsync.session.Controller
import org.walletconnect.walletconnectv2.clientsync.session.Session
import org.walletconnect.walletconnectv2.clientsync.session.SettledSessionPermissions
import org.walletconnect.walletconnectv2.clientsync.session.SettledSessionSequence
import org.walletconnect.walletconnectv2.clientsync.session.after.PostSettlementSession
import org.walletconnect.walletconnectv2.clientsync.session.after.params.Reason
import org.walletconnect.walletconnectv2.clientsync.session.before.PreSettlementSession
import org.walletconnect.walletconnectv2.clientsync.session.before.proposal.RelayProtocolOptions
import org.walletconnect.walletconnectv2.clientsync.session.before.success.SessionParticipant
import org.walletconnect.walletconnectv2.clientsync.session.common.SessionState
import org.walletconnect.walletconnectv2.common.*
import org.walletconnect.walletconnectv2.crypto.CryptoManager
import org.walletconnect.walletconnectv2.crypto.codec.AuthenticatedEncryptionCodec
import org.walletconnect.walletconnectv2.crypto.data.PublicKey
import org.walletconnect.walletconnectv2.crypto.data.SharedKey
import org.walletconnect.walletconnectv2.crypto.managers.LazySodiumCryptoManager
import org.walletconnect.walletconnectv2.engine.model.EngineData
import org.walletconnect.walletconnectv2.engine.sequence.SequenceLifecycleEvent
import org.walletconnect.walletconnectv2.engine.serailising.encode
import org.walletconnect.walletconnectv2.engine.serailising.toEncryptionPayload
import org.walletconnect.walletconnectv2.engine.serailising.tryDeserialize
import org.walletconnect.walletconnectv2.engine.serailising.trySerialize
import org.walletconnect.walletconnectv2.errors.NoSessionDeletePayloadException
import org.walletconnect.walletconnectv2.errors.NoSessionProposalException
import org.walletconnect.walletconnectv2.errors.NoSessionRequestPayloadException
import org.walletconnect.walletconnectv2.errors.exception
import org.walletconnect.walletconnectv2.exceptionHandler
import org.walletconnect.walletconnectv2.relay.WakuRelayRepository
import org.walletconnect.walletconnectv2.relay.data.jsonrpc.JsonRpcMethod.WC_PAIRING_DELETE
import org.walletconnect.walletconnectv2.relay.data.jsonrpc.JsonRpcMethod.WC_PAIRING_PAYLOAD
import org.walletconnect.walletconnectv2.relay.data.jsonrpc.JsonRpcMethod.WC_PAIRING_PING
import org.walletconnect.walletconnectv2.relay.data.jsonrpc.JsonRpcMethod.WC_SESSION_DELETE
import org.walletconnect.walletconnectv2.relay.data.jsonrpc.JsonRpcMethod.WC_SESSION_PAYLOAD
import org.walletconnect.walletconnectv2.relay.data.jsonrpc.JsonRpcMethod.WC_SESSION_PING
import org.walletconnect.walletconnectv2.relay.data.model.Relay
import org.walletconnect.walletconnectv2.relay.data.model.jsonrpc.JsonRpcRequest
import org.walletconnect.walletconnectv2.scope
import org.walletconnect.walletconnectv2.storage.StorageRepository
import org.walletconnect.walletconnectv2.util.Logger
import org.walletconnect.walletconnectv2.util.generateId
import java.util.*

internal class EngineInteractor {
    //region provide with DI
    // TODO: add logic to check hostName for ws/wss scheme with and without ://
    private lateinit var relayRepository: WakuRelayRepository
    private lateinit var storageRepository: StorageRepository
    private val codec: AuthenticatedEncryptionCodec = AuthenticatedEncryptionCodec()
    private val crypto: CryptoManager = LazySodiumCryptoManager()
    //endregion

    private var metaData: AppMetaData? = null
    private var controllerType = ControllerType.CONTROLLER
    private val _sequenceEvent: MutableStateFlow<SequenceLifecycleEvent> = MutableStateFlow(SequenceLifecycleEvent.Default)
    val sequenceEvent: StateFlow<SequenceLifecycleEvent> = _sequenceEvent

    private var isConnected = MutableStateFlow(false) // TODO: Maybe replace with an enum

    internal fun initialize(engine: EngineFactory) {
        this.metaData = engine.metaData
        this.controllerType = if (engine.isController) ControllerType.CONTROLLER else ControllerType.NON_CONTROLLER
        relayRepository = WakuRelayRepository.initRemote(engine.toRelayInitParams())
        storageRepository = StorageRepository(null, engine.application)

        scope.launch(exceptionHandler) {
            relayRepository.eventsFlow
                .onEach { event: WebSocket.Event ->
                    Logger.log("$event")
                    if (event is WebSocket.Event.OnConnectionOpened<*>) {
                        isConnected.compareAndSet(expect = false, update = true)
                    }
                }
                .filterIsInstance<WebSocket.Event.OnConnectionFailed>()
                .collect { event -> throw event.throwable.exception }
        }

        scope.launch(exceptionHandler) {
            relayRepository.subscriptionRequest.collect { relayRequest ->
                val topic: Topic = relayRequest.subscriptionTopic
                val (sharedKey, selfPublic) = crypto.getKeyAgreement(topic)
                val encryptionPayload = relayRequest.message.toEncryptionPayload()
                val decryptedMessage: String = codec.decrypt(encryptionPayload, sharedKey as SharedKey)

                tryDeserialize<JsonRpcRequest>(decryptedMessage)?.let { request ->
                    when (val rpc = request.method) {
                        WC_PAIRING_PAYLOAD -> onPairingPayload(decryptedMessage, sharedKey, selfPublic as PublicKey)
                        WC_PAIRING_DELETE -> onPairingDelete(decryptedMessage, topic)
                        WC_SESSION_PAYLOAD -> onSessionPayload(decryptedMessage, topic)
                        WC_SESSION_DELETE -> onSessionDelete(decryptedMessage, topic)
                        WC_SESSION_PING, WC_PAIRING_PING -> onPing(topic, request.id)
                        else -> onUnsupported(rpc)
                    }
                }

                //TODO deserialize the peer acknowledgement
                tryDeserialize<Relay.Subscription.JsonRpcError>(decryptedMessage)?.let { exception ->
                    Logger.error("Peer Error: ${exception.error.errorMessage}")
                }
            }

            // Automatically resubscribe to any approved sessions in the DB
            supervisorScope {
                storageRepository.sessions.collect { listOfSessions ->
                    listOfSessions
                        .filterIsInstance<Session.Success>()
                        .filter { session -> session.topic != null }
                        .onEach {
                            relayRepository.subscribe(it.topic!!)
                        }
                }
            }
        }
    }

    internal fun pair(uri: String, onSuccess: (String) -> Unit, onFailure: (Throwable) -> Unit) {
        require(::relayRepository.isInitialized)

        val pairingProposal: Pairing.Proposal = uri.toPairProposal()
        val selfPublicKey: PublicKey = crypto.generateKeyPair()
        val expiry = Expiry((Calendar.getInstance().timeInMillis / 1000) + pairingProposal.ttl.seconds)
        val peerPublicKey = PublicKey(pairingProposal.pairingProposer.publicKey)
        val controllerPublicKey = if (pairingProposal.pairingProposer.controller) {
            peerPublicKey
        } else {
            selfPublicKey
        }
        val settledSequence = settlePairingSequence(
            pairingProposal.relay,
            selfPublicKey,
            peerPublicKey,
            controllerPublicKey,
            expiry
        )
        val preSettlementPairingApprove = pairingProposal.toApprove(generateId(), settledSequence.settledTopic, expiry, selfPublicKey)
        val encodedMessage = trySerialize(preSettlementPairingApprove).encode()
        val settledTopic = settledSequence.settledTopic.value

        isConnected
            .filter { isOnline -> isOnline }  // TODO: Update once enum is in place
            .onEach {
                supervisorScope {
                    relayRepository.subscribe(settledSequence.settledTopic)
                    relayRepository.publish(pairingProposal.topic, encodedMessage) { result ->
                        result.fold(
                            onSuccess = {
                                onSuccess(settledTopic)
                                pairingUpdate(settledSequence)
                            },
                            onFailure = { error -> onFailure(error) }
                        )
                    }
                    cancel()
                }
            }
            .launchIn(scope)
    }

    private fun pairingUpdate(settledSequence: SettledPairingSequence) {
        val pairingUpdate: PostSettlementPairing.PairingUpdate =
            PostSettlementPairing.PairingUpdate(id = generateId(), params = Pairing.UpdateParams(state = PairingState(metaData)))
        val json: String = trySerialize(pairingUpdate)
        val (sharedKey, selfPublic) = crypto.getKeyAgreement(Topic(settledSequence.settledTopic.value))
        val encryptedMessage: String = codec.encrypt(json, sharedKey as SharedKey, selfPublic as PublicKey)
        relayRepository.publish(settledSequence.settledTopic, encryptedMessage) { result ->
            result.fold(
                onSuccess = {  /*TODO update Pairing's metadata in local storage*/ },
                onFailure = { error -> Logger.error("Pairing update error: $error") }
            )
        }
    }

    internal fun approve(
        proposal: EngineData.SessionProposal,
        onSuccess: (EngineData.SettledSession) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        require(::relayRepository.isInitialized)

        val selfPublicKey: PublicKey = crypto.generateKeyPair()
        val peerPublicKey = PublicKey(proposal.proposerPublicKey)
        val sessionState = SessionState(proposal.accounts)
        val expiry = Expiry((Calendar.getInstance().timeInMillis / 1000) + proposal.ttl)
        val settledSession: SettledSessionSequence =
            settleSessionSequence(RelayProtocolOptions(), selfPublicKey, peerPublicKey, expiry, sessionState)
        val sessionApprove = PreSettlementSession.Approve(
            id = generateId(),
            params = Session.Success(
                relay = RelayProtocolOptions(),
                state = settledSession.state,
                expiry = expiry,
                responder = SessionParticipant(selfPublicKey.keyAsHex, metadata = this.metaData)
            )
        )
        val approvalJson: String = trySerialize(sessionApprove)
        val (sharedKey, selfPublic) = crypto.getKeyAgreement(Topic(proposal.topic))
        val encryptedMessage: String = codec.encrypt(approvalJson, sharedKey as SharedKey, selfPublic as PublicKey)

        storageRepository.updateStatusToSessionApproval(peerPublicKey.keyAsHex, selfPublicKey.keyAsHex, settledSession.topic.value, proposal.accounts, expiry.seconds)
        relayRepository.subscribe(settledSession.topic)
        relayRepository.publish(Topic(proposal.topic), encryptedMessage) { result ->
            result.fold(
                onSuccess = { with(proposal) { onSuccess(EngineData.SettledSession(icon, name, url, settledSession.topic.value)) } },
                onFailure = { error -> onFailure(error) }
            )
        }
    }

    internal fun reject(
        reason: String, topic: String,
        onSuccess: (Pair<String, String>) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        require(::relayRepository.isInitialized)

        val sessionReject = PreSettlementSession.Reject(id = generateId(), params = Session.Failure(reason = reason))
        val json: String = trySerialize(sessionReject)
        val (sharedKey, selfPublic) = crypto.getKeyAgreement(Topic(topic))
        val encryptedMessage: String = codec.encrypt(json, sharedKey as SharedKey, selfPublic as PublicKey)
        relayRepository.publish(Topic(topic), encryptedMessage) { result ->
            result.fold(
                onSuccess = { onSuccess(Pair(topic, reason)) },
                onFailure = { error -> onFailure(error) }
            )
        }
    }

    internal fun disconnect(
        topic: String,
        reason: String,
        onSuccess: (Pair<String, String>) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        require(::relayRepository.isInitialized)

        val sessionDelete = PostSettlementSession.SessionDelete(id = generateId(), params = Session.DeleteParams(Reason(message = reason)))
        val json = trySerialize(sessionDelete)
        val (sharedKey, selfPublic) = crypto.getKeyAgreement(Topic(topic))
        val encryptedMessage: String = codec.encrypt(json, sharedKey as SharedKey, selfPublic as PublicKey)
        //TODO Add subscriptionId from local storage + Delete all data from local storage coupled with given session
        crypto.removeKeys(topic)
        storageRepository.delete(selfPublic.keyAsHex)
        relayRepository.unsubscribe(Topic(topic), SubscriptionId("1"))
        relayRepository.publish(Topic(topic), encryptedMessage) { result ->
            result.fold(
                onSuccess = { onSuccess(Pair(topic, reason)) },
                onFailure = { error -> onFailure(error) })
        }
    }

    internal fun respondSessionPayload(
        topic: String, jsonRpcResponse: EngineData.JsonRpcResponse,
        onSuccess: (String) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        require(::relayRepository.isInitialized)

        val json = trySerialize(jsonRpcResponse)
        val (sharedKey, selfPublic) = crypto.getKeyAgreement(Topic(topic))
        val encryptedMessage: String = codec.encrypt(json, sharedKey as SharedKey, selfPublic as PublicKey)
        relayRepository.publish(Topic(topic), encryptedMessage) { result ->
            result.fold(
                onSuccess = { onSuccess(topic) },
                onFailure = { error -> onFailure(error) })
        }
    }

    internal fun update(
        topic: String, sessionState: EngineData.SessionState,
        onSuccess: (Pair<String, List<String>>) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        require(::relayRepository.isInitialized)

        val sessionUpdate: PostSettlementSession.SessionUpdate =
            PostSettlementSession.SessionUpdate(id = generateId(), params = Session.UpdateParams(SessionState(sessionState.accounts)))
        val json = trySerialize(sessionUpdate)
        val (sharedKey, selfPublic) = crypto.getKeyAgreement(Topic(topic))
        val encryptedMessage: String = codec.encrypt(json, sharedKey as SharedKey, selfPublic as PublicKey)
        //TODO update the session in local storage
        relayRepository.publish(Topic(topic), encryptedMessage) { result ->
            result.fold(
                onSuccess = { onSuccess(Pair(topic, sessionState.accounts)) },
                onFailure = { error -> onFailure(error) })

        }
    }

    internal fun upgrade(
        topic: String, permissions: EngineData.SessionPermissions,
        onSuccess: (Pair<String, EngineData.SessionPermissions>) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        require(::relayRepository.isInitialized)

        val sessionUpgrade =
            PostSettlementSession.SessionUpgrade(
                id = generateId(),
                params = Session.SessionPermissionsParams(permissions = permissions.toSessionsPermissions())
            )
        val json = trySerialize(sessionUpgrade)
        val (sharedKey, selfPublic) = crypto.getKeyAgreement(Topic(topic))
        val encryptedMessage: String = codec.encrypt(json, sharedKey as SharedKey, selfPublic as PublicKey)
        //TODO update session in local storage
        relayRepository.publish(Topic(topic), encryptedMessage) { result ->
            result.fold(
                onSuccess = { onSuccess(Pair(topic, permissions)) },
                onFailure = { error -> onFailure(error) }
            )
        }
    }

    internal fun ping(topic: String, onSuccess: (String) -> Unit, onFailure: (Throwable) -> Unit) {
        require(::relayRepository.isInitialized)

        /*TODO check whether under given topic there is a pairing or session stored and crated proper Ping class*/
        //val pairingParams = PostSettlementPairing.PairingPing(id = generateId(), params = Pairing.PingParams())
        val sessionPing = PostSettlementSession.SessionPing(id = generateId(), params = Session.PingParams)

        val json = trySerialize(sessionPing)
        val (sharedKey, selfPublic) = crypto.getKeyAgreement(Topic(topic))
        val encryptedMessage: String = codec.encrypt(json, sharedKey as SharedKey, selfPublic as PublicKey)
        relayRepository.publish(Topic(topic), encryptedMessage) { result ->
            result.fold(
                onSuccess = { onSuccess(topic) },
                onFailure = { error -> onFailure(error) }
            )
        }
    }

    private fun onPairingPayload(decryptedMessage: String, sharedKey: SharedKey, selfPublic: PublicKey) {
        tryDeserialize<PostSettlementPairing.PairingPayload>(decryptedMessage)?.let { pairingPayload ->
            val proposal = pairingPayload.payloadParams
            storageRepository.insertSessionProposal(proposal, controllerType)
            //TODO validate session proposal
            crypto.setEncryptionKeys(sharedKey, selfPublic, proposal.topic)
            val sessionProposal = proposal.toSessionProposal()
            _sequenceEvent.value = SequenceLifecycleEvent.OnSessionProposal(sessionProposal)
        } ?: throw NoSessionProposalException()
    }

    private fun onSessionPayload(decryptedMessage: String, topic: Topic) {
        tryDeserialize<PostSettlementSession.SessionPayload>(decryptedMessage)?.let { sessionPayload ->
            //TODO Validate session request + add unmarshaling of generic session request payload to the usable generic object
            val params = sessionPayload.sessionParams
            val chainId = sessionPayload.params.chainId
            val method = sessionPayload.params.request.method
            _sequenceEvent.value = SequenceLifecycleEvent.OnSessionRequest(
                EngineData.SessionRequest(topic.value, chainId, EngineData.SessionRequest.JSONRPCRequest(sessionPayload.id, method, params))
            )
        } ?: throw NoSessionRequestPayloadException()
    }

    private fun onSessionDelete(decryptedMessage: String, topic: Topic) {
        tryDeserialize<PostSettlementSession.SessionDelete>(decryptedMessage)?.let { sessionDelete ->
            //TODO Add subscriptionId from local storage + Delete all data from local storage coupled with given session
            val (_, selfPublic) = crypto.getKeyAgreement(topic)
            storageRepository.delete(selfPublic.keyAsHex)
            crypto.removeKeys(topic.value)
            relayRepository.unsubscribe(topic, SubscriptionId("1"))
            val reason = sessionDelete.message
            _sequenceEvent.value = SequenceLifecycleEvent.OnSessionDeleted(EngineData.DeletedSession(topic.value, reason))
        } ?: throw NoSessionDeletePayloadException()
    }

    private fun onPairingDelete(json: String, topic: Topic) {
        //TODO Add subscriptionId from local storage + Delete all data from local storage coupled with given Pairing
        crypto.removeKeys(topic.value)
        relayRepository.unsubscribe(topic, SubscriptionId("1"))
    }

    private fun onPing(topic: Topic, requestId: Long) {
        val jsonRpcResult = EngineData.JsonRpcResponse.JsonRpcResult(id = requestId, result = "true")
        val json = trySerialize(jsonRpcResult)
        val (sharedKey, selfPublic) = crypto.getKeyAgreement(topic)
        val encryptedMessage: String = codec.encrypt(json, sharedKey as SharedKey, selfPublic as PublicKey)
        relayRepository.publish(topic, encryptedMessage) { result ->
            result.fold(
                onSuccess = {},
                onFailure = { error -> Logger.error("Ping Error: $error") })
        }
    }

    private fun onUnsupported(rpc: String?) {
        Logger.error("Unsupported JsonRpc method: $rpc")
    }

    private fun settlePairingSequence(
        relay: JSONObject,
        selfPublicKey: PublicKey,
        peerPublicKey: PublicKey,
        controllerPublicKey: PublicKey,
        expiry: Expiry
    ): SettledPairingSequence {
        val (_, settledTopic) = crypto.generateTopicAndSharedKey(selfPublicKey, peerPublicKey)
        return SettledPairingSequence(
            settledTopic,
            relay,
            selfPublicKey,
            peerPublicKey,
            PairingPermissions(PairingParticipant(controllerPublicKey.keyAsHex)),
            expiry
        )
    }

    private fun settleSessionSequence(
        relay: RelayProtocolOptions,
        selfPublicKey: PublicKey,
        peerPublicKey: PublicKey,
        expiry: Expiry,
        sessionState: SessionState
    ): SettledSessionSequence {
        val (sharedKey, topic) = crypto.generateTopicAndSharedKey(selfPublicKey, peerPublicKey)
        return SettledSessionSequence(
            topic,
            relay,
            selfPublicKey,
            peerPublicKey,
            SettledSessionPermissions(Controller(selfPublicKey.keyAsHex)),
            sharedKey,
            expiry,
            sessionState
        )
    }

    class EngineFactory(
        val useTLs: Boolean = false,
        val hostName: String,
        val apiKey: String,
        val isController: Boolean,
        val application: Application,
        val metaData: AppMetaData
    )
}