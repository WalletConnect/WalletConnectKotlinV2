package org.walletconnect.walletconnectv2.engine

import android.app.Application
import com.tinder.scarlet.WebSocket
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import org.json.JSONObject
import org.walletconnect.walletconnectv2.clientsync.pairing.SettledPairingSequence
import org.walletconnect.walletconnectv2.clientsync.pairing.before.proposal.PairingProposedPermissions
import org.walletconnect.walletconnectv2.clientsync.session.Session
import org.walletconnect.walletconnectv2.clientsync.session.SettledSessionSequence
import org.walletconnect.walletconnectv2.clientsync.session.after.PostSettlementSession
import org.walletconnect.walletconnectv2.clientsync.session.after.params.Reason
import org.walletconnect.walletconnectv2.clientsync.session.before.PreSettlementSession
import org.walletconnect.walletconnectv2.clientsync.session.before.proposal.RelayProtocolOptions
import org.walletconnect.walletconnectv2.clientsync.session.before.success.SessionParticipant
import org.walletconnect.walletconnectv2.clientsync.session.before.success.SessionState
import org.walletconnect.walletconnectv2.common.*
import org.walletconnect.walletconnectv2.crypto.CryptoManager
import org.walletconnect.walletconnectv2.crypto.codec.AuthenticatedEncryptionCodec
import org.walletconnect.walletconnectv2.crypto.data.PublicKey
import org.walletconnect.walletconnectv2.crypto.managers.LazySodiumCryptoManager
import org.walletconnect.walletconnectv2.engine.model.EngineData
import org.walletconnect.walletconnectv2.engine.sequence.*
import org.walletconnect.walletconnectv2.errors.NoSessionProposalException
import org.walletconnect.walletconnectv2.errors.NoSessionRequestPayloadException
import org.walletconnect.walletconnectv2.errors.exception
import org.walletconnect.walletconnectv2.exceptionHandler
import org.walletconnect.walletconnectv2.keyChain
import org.walletconnect.walletconnectv2.relay.WakuRelayRepository
import org.walletconnect.walletconnectv2.relay.data.jsonrpc.JsonRpcMethod.WC_PAIRING_PAYLOAD
import org.walletconnect.walletconnectv2.relay.data.jsonrpc.JsonRpcMethod.WC_SESSION_DELETE
import org.walletconnect.walletconnectv2.relay.data.jsonrpc.JsonRpcMethod.WC_SESSION_PAYLOAD
import org.walletconnect.walletconnectv2.scope
import org.walletconnect.walletconnectv2.util.Logger
import org.walletconnect.walletconnectv2.util.generateId
import java.util.*

internal class EngineInteractor {
    //region provide with DI
    // TODO: add logic to check hostName for ws/wss scheme with and without ://
    private lateinit var relayRepository: WakuRelayRepository
    private val codec: AuthenticatedEncryptionCodec = AuthenticatedEncryptionCodec()
    private val crypto: CryptoManager = LazySodiumCryptoManager(keyChain)
    //endregion

    private var metaData: AppMetaData? = null
    private val _sequenceEvent: MutableStateFlow<SequenceLifecycleEvent> = MutableStateFlow(SequenceLifecycleEvent.Default)
    val sequenceEvent: StateFlow<SequenceLifecycleEvent> = _sequenceEvent

    fun initialize(engine: EngineFactory) {
        this.metaData = engine.metaData
        relayRepository = WakuRelayRepository.initRemote(engine.toRelayInitParams())

        scope.launch(exceptionHandler) {
            relayRepository.eventsFlow
                .onEach { Logger.log("$it") }
                .filterIsInstance<WebSocket.Event.OnConnectionFailed>()
                .collect { event -> throw event.throwable.exception }
        }

        scope.launch(exceptionHandler) {
            relayRepository.subscriptionRequest.collect { relayRequest ->
                val topic: Topic = relayRequest.subscriptionTopic
                val (sharedKey, selfPublic) = crypto.getKeyAgreement(topic)
                val json: String = codec.decrypt(relayRequest.encryptionPayload, sharedKey)
                when (val rpc = relayRepository.parseToParamsRequest(json)?.method) {
                    WC_PAIRING_PAYLOAD -> onPairingPayload(json, sharedKey, selfPublic)
                    WC_SESSION_PAYLOAD -> onSessionPayload(json, topic)
                    WC_SESSION_DELETE -> onSessionDelete(json, topic)
                    else -> onUnsupported(rpc)
                }
            }
        }
    }

    fun pair(uri: String) {
        require(::relayRepository.isInitialized)

        val pairingProposal = uri.toPairProposal()
        val selfPublicKey = crypto.generateKeyPair()
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

        val preSettlementPairingApprove =
            pairingProposal.toApprove(
                generateId(),
                settledSequence.settledTopic,
                expiry,
                selfPublicKey
            )

        relayRepository.eventsFlow
            .filterIsInstance<WebSocket.Event.OnConnectionOpened<*>>()
            .onEach {
                supervisorScope {
                    relayRepository.subscribe(settledSequence.settledTopic)
                    relayRepository.publishPairingApproval(pairingProposal.topic, preSettlementPairingApprove)
                    cancel()
                }
            }
            .launchIn(scope)
    }

    internal fun approve(proposal: EngineData.SessionProposal, accounts: List<String>) {
        require(::relayRepository.isInitialized)
        val selfPublicKey: PublicKey = crypto.generateKeyPair()
        val peerPublicKey = PublicKey(proposal.proposerPublicKey)
        val sessionState = SessionState(accounts)
        val expiry = Expiry((Calendar.getInstance().timeInMillis / 1000) + proposal.ttl)
        val settledSession: SettledSessionSequence = settleSessionSequence(
            RelayProtocolOptions(),
            selfPublicKey,
            peerPublicKey,
            expiry,
            sessionState
        )

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

        val approvalJson: String = relayRepository.getSessionApprovalJson(sessionApprove)
        val (sharedKey, selfPublic) = crypto.getKeyAgreement(Topic(proposal.topic))
        val encryptedMessage: String = codec.encrypt(approvalJson, sharedKey, selfPublic)

        relayRepository.subscribe(settledSession.topic)
        relayRepository.publish(Topic(proposal.topic), encryptedMessage)

        with(proposal) {
            _sequenceEvent.value =
                SequenceLifecycleEvent.OnSessionSettled(EngineData.SettledSession(icon, name, url, settledSession.topic.topicValue))
        }
    }

    fun reject(reason: String, topic: String) {
        require(::relayRepository.isInitialized)
        val sessionReject = PreSettlementSession.Reject(
            id = generateId(),
            params = Session.Failure(reason = reason)
        )
        val json: String = relayRepository.getSessionRejectionJson(sessionReject)
        val (sharedKey, selfPublic) = crypto.getKeyAgreement(Topic(topic))
        val encryptedMessage: String = codec.encrypt(json, sharedKey, selfPublic)
        relayRepository.publish(Topic(topic), encryptedMessage)
    }

    fun disconnect(topic: String, reason: String) {
        require(::relayRepository.isInitialized)
        val sessionDelete = PostSettlementSession.SessionDelete(id = generateId(), params = Session.DeleteParams(Reason(message = reason)))
        val json = relayRepository.getSessionDeleteJson(sessionDelete)
        val (sharedKey, selfPublic) = crypto.getKeyAgreement(Topic(topic))
        val encryptedMessage: String = codec.encrypt(json, sharedKey, selfPublic)
        //TODO Add subscriptionId from local storage
        //TODO Delete all data from local storage coupled with given session
        relayRepository.unsubscribe(Topic(topic), SubscriptionId("1"))
        relayRepository.publish(Topic(topic), encryptedMessage)
    }

    private fun onPairingPayload(json: String, sharedKey: String, selfPublic: PublicKey) {
        val pairingPayload = relayRepository.parseToPairingPayload(json)
        val proposal = pairingPayload?.payloadParams ?: throw NoSessionProposalException()
        //TODO validate session proposal
        crypto.setEncryptionKeys(sharedKey, selfPublic, proposal.topic)
        val sessionProposal = proposal.toSessionProposal()
        _sequenceEvent.value = SequenceLifecycleEvent.OnSessionProposal(sessionProposal)
    }

    private fun onSessionPayload(json: String, topic: Topic) {
        val sessionPayload = relayRepository.parseToSessionPayload(json)
        val request = sessionPayload?.sessionParams ?: throw NoSessionRequestPayloadException()
        val chainId = sessionPayload.params.chainId
        val method = sessionPayload.params.request.method
        //TODO Validate session request + add unmarshalling of generic session request payload to the usable generic object
        _sequenceEvent.value =
            SequenceLifecycleEvent.OnSessionRequest(EngineData.SessionRequest(topic.topicValue, request, chainId, method))
    }

    private fun onSessionDelete(json: String, topic: Topic) {
        //TODO Delete all data from local storage coupled with given session
        val sessionDelete = relayRepository.parseToSessionDelete(json)
        val reason = sessionDelete?.message ?: ""
        _sequenceEvent.value = SequenceLifecycleEvent.OnSessionDeleted(topic.topicValue, reason)
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