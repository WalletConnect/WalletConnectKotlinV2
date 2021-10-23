package org.walletconnect.walletconnectv2.engine

import android.app.Application
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import org.json.JSONObject
import org.walletconnect.walletconnectv2.WalletConnectScope.scope
import org.walletconnect.walletconnectv2.client.SessionProposal
import org.walletconnect.walletconnectv2.clientcomm.PreSettlementSession
import org.walletconnect.walletconnectv2.clientcomm.pairing.SettledPairingSequence
import org.walletconnect.walletconnectv2.clientcomm.pairing.proposal.PairingProposedPermissions
import org.walletconnect.walletconnectv2.clientcomm.session.RelayProtocolOptions
import org.walletconnect.walletconnectv2.clientcomm.session.Session
import org.walletconnect.walletconnectv2.clientcomm.session.SettledSessionSequence
import org.walletconnect.walletconnectv2.clientcomm.session.proposal.AppMetaData
import org.walletconnect.walletconnectv2.clientcomm.session.success.SessionParticipant
import org.walletconnect.walletconnectv2.clientcomm.session.success.SessionState
import org.walletconnect.walletconnectv2.common.Expiry
import org.walletconnect.walletconnectv2.common.Topic
import org.walletconnect.walletconnectv2.common.toApprove
import org.walletconnect.walletconnectv2.common.toPairProposal
import org.walletconnect.walletconnectv2.crypto.CryptoManager
import org.walletconnect.walletconnectv2.crypto.KeyChain
import org.walletconnect.walletconnectv2.crypto.codec.AuthenticatedEncryptionCodec
import org.walletconnect.walletconnectv2.crypto.data.EncryptionPayload
import org.walletconnect.walletconnectv2.crypto.data.PublicKey
import org.walletconnect.walletconnectv2.crypto.managers.LazySodiumCryptoManager
import org.walletconnect.walletconnectv2.relay.WakuRelayRepository
import org.walletconnect.walletconnectv2.util.generateId
import org.walletconnect.walletconnectv2.util.toEncryptionPayload
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

    private val _sessionProposal: MutableStateFlow<Session.Proposal?> = MutableStateFlow(null)
    val sessionProposal: StateFlow<Session.Proposal?> = _sessionProposal

    //todo create topic -> keys map
    private var pairingPublicKey = PublicKey("")
    private var peerPublicKey = PublicKey("")
    var pairingSharedKey: String = ""

    fun initialize(engineFactory: EngineFactory) {
        relayRepository = WakuRelayRepository.initRemote(
            engineFactory.useTLs,
            engineFactory.hostName,
            engineFactory.port,
            engineFactory.application
        )

        scope.launch {
            relayRepository.subscriptionRequest.collect {

                supervisorScope {
                    println("PUBLISH")
                    relayRepository.publishSessionProposalAcknowledgment(it.id)
                }

                val pairingPayloadJson = codec.decrypt(
                    it.params.subscriptionData.message.toEncryptionPayload(),
                    crypto.getSharedKey(pairingPublicKey, peerPublicKey)
                )
                val pairingPayload = relayRepository.parseToPairingPayload(pairingPayloadJson)
                val sessionProposal = pairingPayload?.params?.request?.params
                _sessionProposal.value = sessionProposal
            }
        }
    }

    fun pair(uri: String) {
        require(::relayRepository.isInitialized)
        val pairingProposal = uri.toPairProposal()
        val selfPublicKey = crypto.generateKeyPair().also { pairingPublicKey = it }
        val expiry =
            Expiry((Calendar.getInstance().timeInMillis / 1000) + pairingProposal.ttl.seconds)
        val peerPublicKey =
            PublicKey(pairingProposal.pairingProposer.publicKey).also { peerPublicKey = it }
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

    private fun settlePairingSequence(
        relay: JSONObject,
        selfPublicKey: PublicKey,
        peerPublicKey: PublicKey,
        permissions: PairingProposedPermissions?,
        controllerPublicKey: PublicKey,
        expiry: Expiry
    ): SettledPairingSequence {
        require(::relayRepository.isInitialized)
        val (sharedKey, settledTopic) =
            crypto.generateTopicAndSharedKey(selfPublicKey, peerPublicKey)
        pairingSharedKey = sharedKey
        return SettledPairingSequence(
            settledTopic,
            relay,
            selfPublicKey,
            peerPublicKey,
            permissions to controllerPublicKey,
            expiry
        )
    }

    fun approve(proposal: SessionProposal) {
        require(::relayRepository.isInitialized)
        val selfPublicKey: PublicKey = crypto.generateKeyPair()
        val peerPublicKey = PublicKey(proposal.proposerPublicKey)
        val sessionState = SessionState(proposal.accounts)
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
                    metadata = AppMetaData(name = "Kotlin Wallet")
                )
            )
        )

        val sessionApprovalJson: String =
            relayRepository.getSessionApprovalJson(preSettlementSession)

        val encryptedJson: EncryptionPayload = codec.encrypt(
            sessionApprovalJson,
            pairingSharedKey,
            pairingPublicKey
        )

        val encryptedString =
            encryptedJson.iv + encryptedJson.publicKey + encryptedJson.mac + encryptedJson.cipherText
        relayRepository.publishSessionApproval(Topic(proposal.topic), encryptedString)
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
        val port: Int = 0,
        val application: Application
    )
}