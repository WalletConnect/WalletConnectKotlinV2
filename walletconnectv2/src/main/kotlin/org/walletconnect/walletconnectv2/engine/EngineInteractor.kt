package org.walletconnect.walletconnectv2.engine

import android.app.Application
import com.tinder.scarlet.WebSocket
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.walletconnect.walletconnectv2.clientsync.pairing.SettledPairingSequence
import org.walletconnect.walletconnectv2.clientsync.pairing.before.proposal.PairingProposedPermissions
import org.walletconnect.walletconnectv2.clientsync.session.Session
import org.walletconnect.walletconnectv2.clientsync.session.SettledSessionSequence
import org.walletconnect.walletconnectv2.clientsync.session.before.PreSettlementSession
import org.walletconnect.walletconnectv2.clientsync.session.before.proposal.RelayProtocolOptions
import org.walletconnect.walletconnectv2.clientsync.session.before.success.SessionParticipant
import org.walletconnect.walletconnectv2.clientsync.session.before.success.SessionState
import org.walletconnect.walletconnectv2.common.*
import org.walletconnect.walletconnectv2.crypto.CryptoManager
import org.walletconnect.walletconnectv2.crypto.codec.AuthenticatedEncryptionCodec
import org.walletconnect.walletconnectv2.crypto.data.PublicKey
import org.walletconnect.walletconnectv2.crypto.managers.LazySodiumCryptoManager
import org.walletconnect.walletconnectv2.engine.jsonrpc.Default
import org.walletconnect.walletconnectv2.engine.jsonrpc.JsonRpcEvent
import org.walletconnect.walletconnectv2.engine.jsonrpc.OnSessionProposal
import org.walletconnect.walletconnectv2.engine.jsonrpc.OnSessionRequest
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
import org.walletconnect.walletconnectv2.util.generateId
import timber.log.Timber
import java.util.*

class EngineInteractor {
    //region provide with DI
    // TODO: add logic to check hostName for ws/wss scheme with and without ://
    private lateinit var relayRepository: WakuRelayRepository
    private val codec: AuthenticatedEncryptionCodec = AuthenticatedEncryptionCodec()
    private val crypto: CryptoManager = LazySodiumCryptoManager(keyChain)
    //endregion

    private var metaData: AppMetaData? = null
    private val _jsonRpcEvents: MutableStateFlow<JsonRpcEvent> = MutableStateFlow(Default)
    val jsonRpcEvents: StateFlow<JsonRpcEvent> = _jsonRpcEvents

    fun initialize(engine: EngineFactory) {
        this.metaData = engine.metaData
        relayRepository = WakuRelayRepository.initRemote(engine.toRelayInitParams())

        scope.launch(exceptionHandler) {
            relayRepository.eventsFlow
                .onEach { Timber.tag("WalletConnect connection event").d("$it") }
                .filterIsInstance<WebSocket.Event.OnConnectionFailed>()
                .collect { event -> throw event.throwable.exception }
        }

        scope.launch(exceptionHandler) {
            relayRepository.subscriptionRequest().collect { relayRequest ->
                val (sharedKey, selfPublic) = crypto.getKeyAgreement(relayRequest.subscriptionTopic)
                val json: String = codec.decrypt(relayRequest.encryptionPayload, sharedKey)
                when (val rpc = relayRepository.parseToParamsRequest(json)?.method) {
                    WC_PAIRING_PAYLOAD -> onPairingPayload(json, sharedKey, selfPublic)
                    WC_SESSION_PAYLOAD -> onSessionRequest(json)
                    WC_SESSION_DELETE -> onSessionDelete()
                    else -> onUnsupported(rpc)
                }
            }
        }
    }

    fun pair(uri: String) {
        require(::relayRepository.isInitialized)
        val pairingProposal = uri.toPairProposal()
        val selfPublicKey = crypto.generateKeyPair()
        val expiry =
            Expiry((Calendar.getInstance().timeInMillis / 1000) + pairingProposal.ttl.seconds)
        val peerPublicKey =
            PublicKey(pairingProposal.pairingProposer.publicKey)
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

        relayRepository.subscribe(settledSequence.settledTopic)
        relayRepository.publishPairingApproval(pairingProposal.topic, preSettlementPairingApprove)
    }

    fun approve(accounts: List<String>, proposerPublicKey: String, ttl: Long, topic: String) {
        require(::relayRepository.isInitialized)
        val selfPublicKey: PublicKey = crypto.generateKeyPair()
        val peerPublicKey = PublicKey(proposerPublicKey)
        val sessionState = SessionState(accounts)
        val expiry = Expiry((Calendar.getInstance().timeInMillis / 1000) + ttl)

        val settledSession: SettledSessionSequence = settleSessionSequence(
            RelayProtocolOptions(),
            selfPublicKey,
            peerPublicKey,
            expiry,
            sessionState
        )

        val preSettlementSession = PreSettlementSession.Approve(
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

        val approvalJson: String = relayRepository.getSessionApprovalJson(preSettlementSession)
        val (sharedKey, selfPublic) = crypto.getKeyAgreement(Topic(topic))
        val encryptedMessage: String = codec.encrypt(approvalJson, sharedKey, selfPublic)
        relayRepository.subscribe(settledSession.settledTopic)
        relayRepository.publish(Topic(topic), encryptedMessage)
    }

    fun reject(reason: String, topic: String) {
        require(::relayRepository.isInitialized)
        val preSettlementSession =
            PreSettlementSession.Reject(
                id = generateId(),
                params = Session.Failure(reason = reason)
            )
        val json: String = relayRepository.getSessionRejectionJson(preSettlementSession)
        val (sharedKey, selfPublic) = crypto.getKeyAgreement(Topic(topic))
        val encryptedMessage: String = codec.encrypt(json, sharedKey, selfPublic)
        relayRepository.publish(Topic(topic), encryptedMessage)
    }

    private fun onPairingPayload(json: String, sharedKey: String, selfPublic: PublicKey) {
        val pairingPayload = relayRepository.parseToPairingPayload(json)
        val proposal = pairingPayload?.payloadParams ?: throw NoSessionProposalException()
        crypto.setEncryptionKeys(sharedKey, selfPublic, proposal.topic)
        //TODO validate session proposal
        val sessionProposal = proposal.toSessionProposal()
        _jsonRpcEvents.value = OnSessionProposal(sessionProposal)
    }

    private fun onSessionRequest(json: String) {
        val sessionPayload = relayRepository.parseToSessionPayload(json)
        val params = sessionPayload?.sessionParams ?: throw NoSessionRequestPayloadException()
        //TODO validate session request
        /*TODO add unmarshaling of generic session request payload to the usable generic object
        * then update state flow to return it to the wallet*/

        _jsonRpcEvents.value = OnSessionRequest(params)
    }


    private fun onSessionDelete() {
        //TODO implement me, delete all data coupled with given session
    }

    private fun onUnsupported(rpc: String?) {
        Timber.tag("WalletConnect unsupported RPC").e(rpc)
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
        val (sharedKey, settledTopic) =
            crypto.generateTopicAndSharedKey(selfPublicKey, peerPublicKey)
        return SettledSessionSequence(
            settledTopic,
            relay,
            selfPublicKey,
            peerPublicKey,
            sharedKey,
            expiry,
            sessionState
        )
    }

    data class EngineFactory(
        val useTLs: Boolean = false,
        val hostName: String,
        val apiKey: String,
        val isController: Boolean,
        val application: Application,
        val metaData: AppMetaData
    )
}