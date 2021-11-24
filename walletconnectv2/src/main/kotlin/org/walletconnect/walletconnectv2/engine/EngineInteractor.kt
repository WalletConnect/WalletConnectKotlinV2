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
import org.walletconnect.walletconnectv2.clientsync.pairing.before.proposal.PairingProposedPermissions
import org.walletconnect.walletconnectv2.clientsync.pairing.before.success.PairingState
import org.walletconnect.walletconnectv2.clientsync.session.Session
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
import org.walletconnect.walletconnectv2.engine.serailising.tryDeserialize
import org.walletconnect.walletconnectv2.engine.serailising.trySerialize
import org.walletconnect.walletconnectv2.errors.NoSessionDeletePayloadException
import org.walletconnect.walletconnectv2.errors.NoSessionProposalException
import org.walletconnect.walletconnectv2.errors.NoSessionRequestPayloadException
import org.walletconnect.walletconnectv2.errors.exception
import org.walletconnect.walletconnectv2.exceptionHandler
import org.walletconnect.walletconnectv2.relay.WakuRelayRepository
import org.walletconnect.walletconnectv2.relay.data.jsonrpc.JsonRpcMethod.WC_PAIRING_PAYLOAD
import org.walletconnect.walletconnectv2.relay.data.jsonrpc.JsonRpcMethod.WC_SESSION_DELETE
import org.walletconnect.walletconnectv2.relay.data.jsonrpc.JsonRpcMethod.WC_SESSION_PAYLOAD
import org.walletconnect.walletconnectv2.relay.data.model.Relay
import org.walletconnect.walletconnectv2.relay.data.model.jsonrpc.JsonRpcRequest
import org.walletconnect.walletconnectv2.scope
import org.walletconnect.walletconnectv2.util.Logger
import org.walletconnect.walletconnectv2.util.generateId
import org.walletconnect.walletconnectv2.util.toEncryptionPayload
import timber.log.Timber
import java.util.*

internal class EngineInteractor {
    //region provide with DI
    // TODO: add logic to check hostName for ws/wss scheme with and without ://
    private lateinit var relayRepository: WakuRelayRepository
    private val codec: AuthenticatedEncryptionCodec = AuthenticatedEncryptionCodec()
    private val crypto: CryptoManager = LazySodiumCryptoManager()
    //endregion

    private var metaData: AppMetaData? = null
    private val _sequenceEvent: MutableStateFlow<SequenceLifecycleEvent> = MutableStateFlow(SequenceLifecycleEvent.Default)
    val sequenceEvent: StateFlow<SequenceLifecycleEvent> = _sequenceEvent

    private var isConnected = MutableStateFlow(false) // TODO: Maybe replace with an enum

    internal fun initialize(engine: EngineFactory) {
        this.metaData = engine.metaData
        relayRepository = WakuRelayRepository.initRemote(engine.toRelayInitParams())

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

                Logger.error("Kobe; Peer message: $decryptedMessage")

                tryDeserialize<JsonRpcRequest>(decryptedMessage)?.let { request ->
                    when (val rpc = request.method) {
                        WC_PAIRING_PAYLOAD -> onPairingPayload(decryptedMessage, sharedKey, selfPublic as PublicKey)
                        WC_SESSION_PAYLOAD -> onSessionPayload(decryptedMessage, topic)
                        WC_SESSION_DELETE -> onSessionDelete(decryptedMessage, topic)
                        else -> onUnsupported(rpc)
                    }
                }

                tryDeserialize<Relay.Subscription.JsonRpcError>(decryptedMessage)?.let { exception ->
                    Timber.tag("WalletConnect exception").e(exception.error.errorMessage)
                }
            }
        }
    }

    internal fun pair(uri: String, onResult: (Result<String>) -> Unit) {
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
            pairingProposal.permissions,
            controllerPublicKey,
            expiry
        )

        //Call when success from relay
//        pairingUpdate(settledSequence)

        observePublishAcknowledgement(onResult, settledSequence.settledTopic.topicValue)
        observePublishError(onResult)


        val preSettlementPairingApprove = pairingProposal.toApprove(generateId(), settledSequence.settledTopic, expiry, selfPublicKey)
        //move to codec
        val encodedMessage =
            trySerialize(preSettlementPairingApprove)
                .encodeToByteArray()
                .joinToString(separator = "") { bytes -> String.format("%02X", bytes) }

        isConnected
            .filter { isOnline -> isOnline }  // TODO: Update once enum is in place
            .onEach {
                supervisorScope {
                    relayRepository.subscribe(settledSequence.settledTopic)
                    relayRepository.publish(pairingProposal.topic, encodedMessage)
                    cancel()
                }
            }
            .launchIn(scope)
    }

    private fun pairingUpdate(settledSequence: SettledPairingSequence) {
        val pairingUpdate: PostSettlementPairing.PairingUpdate =
            PostSettlementPairing.PairingUpdate(id = generateId(), params = Pairing.UpdateParams(state = PairingState(metaData)))

        val json: String = trySerialize(pairingUpdate)
        val (sharedKey, selfPublic) = crypto.getKeyAgreement(Topic(settledSequence.settledTopic.topicValue))
        val encryptedMessage: String = codec.encrypt(json, sharedKey as SharedKey, selfPublic as PublicKey)
        relayRepository.publish(settledSequence.settledTopic, encryptedMessage)
    }

    internal fun approve(
        proposal: EngineData.SessionProposal,
        accounts: List<String>,
        onResult: (Result<EngineData.SettledSession>) -> Unit
    ) {
        require(::relayRepository.isInitialized)

        val selfPublicKey: PublicKey = crypto.generateKeyPair()
        val peerPublicKey = PublicKey(proposal.proposerPublicKey)
        val sessionState = SessionState(accounts)
        val expiry = Expiry((Calendar.getInstance().timeInMillis / 1000) + proposal.ttl)
        val settledSession: SettledSessionSequence =
            settleSessionSequence(RelayProtocolOptions(), selfPublicKey, peerPublicKey, expiry, sessionState)

        val sessionApprove = PreSettlementSession.Approve(
            id = generateId(),
            params = Session.Success(
                relay = RelayProtocolOptions(),
                state = settledSession.state,
                expiry = expiry,
                responder = SessionParticipant(
                    selfPublicKey.keyAsHex,
                    metadata = this.metaData
                )
            )
        )

        val approvalJson: String = trySerialize(sessionApprove)
        val (sharedKey, selfPublic) = crypto.getKeyAgreement(Topic(proposal.topic))
        val encryptedMessage: String = codec.encrypt(approvalJson, sharedKey as SharedKey, selfPublic as PublicKey)

        with(proposal) {
            observePublishAcknowledgement(onResult, EngineData.SettledSession(icon, name, url, settledSession.topic.topicValue))
        }
        observePublishError(onResult)

        relayRepository.subscribe(settledSession.topic)
        relayRepository.publish(Topic(proposal.topic), encryptedMessage)
    }

    internal fun reject(reason: String, topic: String, onResult: (Result<String>) -> Unit) {
        require(::relayRepository.isInitialized)

        val sessionReject = PreSettlementSession.Reject(id = generateId(), params = Session.Failure(reason = reason))
        val json: String = trySerialize(sessionReject)
        val (sharedKey, selfPublic) = crypto.getKeyAgreement(Topic(topic))
        val encryptedMessage: String = codec.encrypt(json, sharedKey as SharedKey, selfPublic as PublicKey)

        observePublishAcknowledgement(onResult, topic)
        observePublishError(onResult)

        relayRepository.publish(Topic(topic), encryptedMessage)
    }

    internal fun disconnect(topic: String, reason: String, onResult: (Result<String>) -> Unit) {
        require(::relayRepository.isInitialized)

        val sessionDelete = PostSettlementSession.SessionDelete(id = generateId(), params = Session.DeleteParams(Reason(message = reason)))
        val json = trySerialize(sessionDelete)
        val (sharedKey, selfPublic) = crypto.getKeyAgreement(Topic(topic))
        val encryptedMessage: String = codec.encrypt(json, sharedKey as SharedKey, selfPublic as PublicKey)

        observePublishAcknowledgement(onResult, topic)
        observePublishError(onResult)

        //TODO Add subscriptionId from local storage + Delete all data from local storage coupled with given session
        crypto.removeKeys(topic)
        relayRepository.unsubscribe(Topic(topic), SubscriptionId("1"))
        relayRepository.publish(Topic(topic), encryptedMessage)
    }

    internal fun respondSessionPayload(topic: String, jsonRpcResponse: EngineData.JsonRpcResponse, onResult: (Result<String>) -> Unit) {
        require(::relayRepository.isInitialized)

        val json = trySerialize(jsonRpcResponse)
        val (sharedKey, selfPublic) = crypto.getKeyAgreement(Topic(topic))
        val encryptedMessage: String = codec.encrypt(json, sharedKey as SharedKey, selfPublic as PublicKey)
        observePublishAcknowledgement(onResult, topic)
        observePublishError(onResult)
        relayRepository.publish(Topic(topic), encryptedMessage)
    }

    internal fun sessionUpdate(
        topic: String,
        sessionState: EngineData.SessionState,
        onResult: (Result<Pair<String, List<String>>>) -> Unit
    ) {
        require(::relayRepository.isInitialized)

        val sessionUpdate: PostSettlementSession.SessionUpdate =
            PostSettlementSession.SessionUpdate(id = generateId(), params = Session.UpdateParams(SessionState(sessionState.accounts)))
        val json = trySerialize(sessionUpdate)
        val (sharedKey, selfPublic) = crypto.getKeyAgreement(Topic(topic))
        val encryptedMessage: String = codec.encrypt(json, sharedKey as SharedKey, selfPublic as PublicKey)
        observePublishAcknowledgement(onResult, Pair(topic, sessionState.accounts))
        observePublishError(onResult)

        //TODO update the session in local storage
        relayRepository.publish(Topic(topic), encryptedMessage)
    }

    private fun <T> observePublishError(onResult: (Result<T>) -> Unit) {
        scope.launch {
            relayRepository.observePublishResponseError
                .onEach { jsonRpcError -> Logger.error(Throwable(jsonRpcError.error.errorMessage)) }
                .catch { exception -> Logger.error(exception) }
                .collect { errorResponse ->
                    supervisorScope {
                        onResult(Result.failure(Throwable(errorResponse.error.errorMessage)))
                        cancel()
                    }
                }
        }
    }

    private fun <T> observePublishAcknowledgement(onResult: (Result<T>) -> Unit, result: T) {
        scope.launch {
            relayRepository.observePublishAcknowledgement
                .catch { exception -> Logger.error(exception) }
                .collect {
                    supervisorScope {
                        onResult(Result.success(result))
                        cancel()
                    }
                }
        }
    }

    private fun onPairingPayload(json: String, sharedKey: SharedKey, selfPublic: PublicKey) {
        tryDeserialize<PostSettlementPairing.PairingPayload>(json)?.let { pairingPayload ->
            val proposal = pairingPayload.payloadParams
            //TODO validate session proposal
            crypto.setEncryptionKeys(sharedKey, selfPublic, proposal.topic)
            val sessionProposal = proposal.toSessionProposal()
            _sequenceEvent.value = SequenceLifecycleEvent.OnSessionProposal(sessionProposal)
        } ?: throw NoSessionProposalException()
    }

    private fun onSessionPayload(json: String, topic: Topic) {
        tryDeserialize<PostSettlementSession.SessionPayload>(json)?.let { sessionPayload ->
            //TODO Validate session request + add unmarshaling of generic session request payload to the usable generic object
            val params = sessionPayload.sessionParams
            val chainId = sessionPayload.params.chainId
            val method = sessionPayload.params.request.method
            _sequenceEvent.value =
                SequenceLifecycleEvent.OnSessionRequest(
                    EngineData.SessionRequest(
                        topic.topicValue,
                        chainId,
                        EngineData.JSONRPCRequest(sessionPayload.id, method, params)
                    )
                )

        } ?: throw NoSessionRequestPayloadException()
    }

    private fun onSessionDelete(json: String, topic: Topic) {
        tryDeserialize<PostSettlementSession.SessionDelete>(json)?.let { sessionDelete ->
            //TODO Add subscriptionId from local storage + Delete all data from local storage coupled with given session
            crypto.removeKeys(topic.topicValue)
            relayRepository.unsubscribe(topic, SubscriptionId("1"))
            val reason = sessionDelete.message
            _sequenceEvent.value = SequenceLifecycleEvent.OnSessionDeleted(topic.topicValue, reason)
        } ?: throw NoSessionDeletePayloadException()
    }

    private fun onUnsupported(rpc: String?) {
        Logger.error(rpc)
    }

    private fun settlePairingSequence(
        relay: JSONObject,
        selfPublicKey: PublicKey,
        peerPublicKey: PublicKey,
        permissions: PairingProposedPermissions?,
        controllerPublicKey: PublicKey,
        expiry: Expiry
    ): SettledPairingSequence {
        val (_, settledTopic) = crypto.generateTopicAndSharedKey(selfPublicKey, peerPublicKey)
        return SettledPairingSequence(
            settledTopic,
            relay,
            selfPublicKey,
            peerPublicKey,
            permissions to controllerPublicKey,
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