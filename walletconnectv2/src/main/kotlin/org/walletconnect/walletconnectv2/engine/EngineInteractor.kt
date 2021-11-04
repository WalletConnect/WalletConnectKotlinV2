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
import org.walletconnect.walletconnectv2.crypto.data.PublicKey
import org.walletconnect.walletconnectv2.crypto.managers.LazySodiumCryptoManager
import org.walletconnect.walletconnectv2.engine.jsonrpc.*
import org.walletconnect.walletconnectv2.errors.exception
import org.walletconnect.walletconnectv2.exceptionHandler
import org.walletconnect.walletconnectv2.keyChain
import org.walletconnect.walletconnectv2.relay.*
import org.walletconnect.walletconnectv2.scope
import org.walletconnect.walletconnectv2.util.generateId
import timber.log.Timber
import java.util.*

class EngineInteractor : JsonRpcHandler {
    //region provide with DI
    // TODO: add logic to check hostName for ws/wss scheme with and without ://
    private lateinit var relayRepository: WakuRelayRepository
    private val crypto: CryptoManager = LazySodiumCryptoManager(keyChain)
    //endregion

    private var metaData: AppMetaData? = null
    private val _jsonRpcEvents: MutableStateFlow<JsonRpcEvent> = MutableStateFlow(Default)
    val jsonRpcEvents: StateFlow<JsonRpcEvent> = _jsonRpcEvents

    fun initialize(engine: EngineFactory) {
        this.metaData = engine.metaData

        with(engine) {
            relayRepository =
                WakuRelayRepository.initRemote(toRelayInitParams(), this@EngineInteractor)
        }

        scope.launch(exceptionHandler) {
            relayRepository.eventsFlow
                .map { Timber.tag("WalletConnect connection event").d("$it") }
                .filterIsInstance<WebSocket.Event.OnConnectionFailed>()
                .collect { event -> throw event.throwable.exception }
        }

        scope.launch(exceptionHandler) { relayRepository.subscriptionRequest().collect() }
    }

    override var onSessionPropose: (proposal: Session.Proposal) -> Unit = { proposal ->
        val sessionProposal = proposal.toSessionProposal()
        _jsonRpcEvents.value = OnSessionProposal(sessionProposal)
    }

    override var onSessionRequest: (payload: Any) -> Unit = { payload ->
        /*TODO add unmarshaling of generic session request payload to the usable generic object
        * then update state flow to return it to the wallet*/
        _jsonRpcEvents.value = OnSessionRequest("HEHE")
    }

    override var onSessionDelete: () -> Unit = {
        //TODO implement me, delete all data coupled with given session
    }

    override var onUnsupported: (rpc: String?) -> Unit = { rpc ->
        Timber.tag("WalletConnect unsupported RPC").e(rpc)
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

    fun approve(
        accounts: List<String>,
        proposerPublicKey: String,
        proposalTtl: Long,
        proposalTopic: String
    ) {
        require(::relayRepository.isInitialized)
        val selfPublicKey: PublicKey = crypto.generateKeyPair()
        val peerPublicKey = PublicKey(proposerPublicKey)
        val sessionState = SessionState(accounts)
        val expiry = Expiry((Calendar.getInstance().timeInMillis / 1000) + proposalTtl)

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

        val sessionApprovalJson: String =
            relayRepository.getSessionApprovalJson(preSettlementSession)

        relayRepository.subscribe(settledSession.settledTopic)
        relayRepository.publish(Topic(proposalTopic), sessionApprovalJson)
    }

    fun reject(reason: String, proposalTopic: String) {
        require(::relayRepository.isInitialized)
        val preSettlementSession =
            PreSettlementSession.Reject(
                id = generateId(),
                params = Session.Failure(reason = reason)
            )
        val sessionRejectionJson: String =
            relayRepository.getSessionRejectionJson(preSettlementSession)
        relayRepository.publish(Topic(proposalTopic), sessionRejectionJson)
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