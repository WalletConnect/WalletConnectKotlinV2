package org.walletconnect.walletconnectv2.engine

import android.app.Application
import com.tinder.scarlet.WebSocket
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import org.json.JSONObject
import org.walletconnect.walletconnectv2.WalletConnectScope.exceptionHandler
import org.walletconnect.walletconnectv2.WalletConnectScope.scope
import org.walletconnect.walletconnectv2.client.SessionProposal
import org.walletconnect.walletconnectv2.clientsync.PreSettlementSession
import org.walletconnect.walletconnectv2.clientsync.pairing.SettledPairingSequence
import org.walletconnect.walletconnectv2.clientsync.pairing.proposal.PairingProposedPermissions
import org.walletconnect.walletconnectv2.clientsync.session.Session
import org.walletconnect.walletconnectv2.clientsync.session.SettledSessionSequence
import org.walletconnect.walletconnectv2.clientsync.session.proposal.RelayProtocolOptions
import org.walletconnect.walletconnectv2.clientsync.session.success.SessionParticipant
import org.walletconnect.walletconnectv2.clientsync.session.success.SessionState
import org.walletconnect.walletconnectv2.common.*
import org.walletconnect.walletconnectv2.crypto.CryptoManager
import org.walletconnect.walletconnectv2.crypto.KeyChain
import org.walletconnect.walletconnectv2.crypto.codec.AuthenticatedEncryptionCodec
import org.walletconnect.walletconnectv2.crypto.data.EncryptionPayload
import org.walletconnect.walletconnectv2.crypto.data.PublicKey
import org.walletconnect.walletconnectv2.crypto.managers.LazySodiumCryptoManager
import org.walletconnect.walletconnectv2.errors.NoSessionProposalException
import org.walletconnect.walletconnectv2.errors.exception
import org.walletconnect.walletconnectv2.relay.WakuRelayRepository
import org.walletconnect.walletconnectv2.util.generateId
import java.util.*

class EngineInteractor {
    //region provide with DI
    // TODO: add logic to check hostName for ws/wss scheme with and without ://
    private lateinit var relayRepository: WakuRelayRepository

    private val keyChain = object : KeyChain {
        val mapOfKeys = mutableMapOf<String, String>()

        override fun setKey(key: String, value: String) {
            mapOfKeys[key] = value
        }

        override fun getKey(key: String): String {
            return mapOfKeys[key]!!
        }
    }
    private val crypto: CryptoManager = LazySodiumCryptoManager(keyChain)
    private val codec: AuthenticatedEncryptionCodec = AuthenticatedEncryptionCodec()

    //endregion
    private var metaData: AppMetaData? = null
    private val _sessionProposal: MutableStateFlow<Session.Proposal?> = MutableStateFlow(null)
    val sessionProposal: StateFlow<Session.Proposal?> = _sessionProposal

    fun initialize(engine: EngineFactory) {
        this.metaData = engine.metaData
        relayRepository = WakuRelayRepository.initRemote(
            engine.useTLs,
            engine.hostName,
            engine.apiKey,
            engine.application
        )

        scope.launch(exceptionHandler) {
            relayRepository.eventsFlow
                .filterIsInstance<WebSocket.Event.OnConnectionFailed>()
                .collect { event ->
                    throw event.throwable.exception
                }
        }

        scope.launch {
            relayRepository.subscriptionRequest.collect { request ->
                supervisorScope {
                    relayRepository.publishSessionProposalAcknowledgment(request.id)
                }

                val (sharedKey, selfPublic) = crypto.getKeyAgreement(request.subscriptionTopic)
                val pairingPayloadJson = codec.decrypt(request.encryptionPayload, sharedKey)
                val pairingPayload = relayRepository.parseToPairingPayload(pairingPayloadJson)
                val sessionProposal: Session.Proposal =
                    pairingPayload?.params?.request?.params ?: throw NoSessionProposalException()
                crypto.setEncryptionKeys(sharedKey, selfPublic, sessionProposal.topic)
                _sessionProposal.value = sessionProposal
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

    fun approve(accounts: List<String>, proposal: SessionProposal) {
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
        val (sharedKey, selfPublic) = crypto.getKeyAgreement(Topic(proposal.topic))
        val encryptedJson: EncryptionPayload =
            codec.encrypt(sessionApprovalJson, sharedKey, selfPublic)
        val encryptedString =
            encryptedJson.iv + encryptedJson.publicKey + encryptedJson.mac + encryptedJson.cipherText

        relayRepository.publish(Topic(proposal.topic), encryptedString)
        //TODO subscribe on topic D and set keys on topic D
    }

    fun reject(reason: String, proposal: SessionProposal) {
        val preSettlementSession =
            PreSettlementSession.Reject(id = generateId(), params = Session.Failure(reason))
        val sessionRejectionJson: String =
            relayRepository.getSessionRejectionJson(preSettlementSession)
        val (sharedKey, selfPublic) = crypto.getKeyAgreement(Topic(proposal.topic))
        val encryptedJson: EncryptionPayload =
            codec.encrypt(sessionRejectionJson, sharedKey, selfPublic)
        val encryptedString =
            encryptedJson.iv + encryptedJson.publicKey + encryptedJson.mac + encryptedJson.cipherText

        relayRepository.publish(Topic(proposal.topic), encryptedString)
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