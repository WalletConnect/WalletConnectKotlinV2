package org.walletconnect.walletconnectv2.engine

import com.tinder.scarlet.Stream
import com.tinder.scarlet.WebSocket
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import org.json.JSONObject
import org.walletconnect.walletconnectv2.common.Expiry
import org.walletconnect.walletconnectv2.common.Topic
import org.walletconnect.walletconnectv2.common.toApprove
import org.walletconnect.walletconnectv2.common.toPairProposal
import org.walletconnect.walletconnectv2.crypto.CryptoManager
import org.walletconnect.walletconnectv2.crypto.KeyChain
import org.walletconnect.walletconnectv2.crypto.data.PublicKey
import org.walletconnect.walletconnectv2.crypto.managers.LazySodiumCryptoManager
import org.walletconnect.walletconnectv2.outofband.pairing.proposal.PairingProposedPermissions
import org.walletconnect.walletconnectv2.relay.WakuRelayRepository
import java.util.*

class EngineInteractor(hostName: String) {
    //region provide with DI
    // TODO: add logic to check hostName for ws/wss scheme with and without ://
    private val relayRepository: WakuRelayRepository = WakuRelayRepository.initRemote(useTLs = true, hostName = hostName)
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
    //endregion

    private var controller = false

    val pairingResponse = relayRepository.publishResponse

    suspend fun pair(uri: String) {
        val pairingProposal = uri.toPairProposal()
        val approved = pairingProposal.pairingProposer.controller != controller
        val selfPublicKey = crypto.generateKeyPair()
        val expiry = Expiry((Calendar.getInstance().timeInMillis / 1000) + pairingProposal.ttl.seconds)

        val peerPublicKey = PublicKey(pairingProposal.pairingProposer.publicKey)
        val controllerPublicKey = if (pairingProposal.pairingProposer.controller) {
            peerPublicKey
        } else {
            selfPublicKey
        }
        val settledSequence = settle(pairingProposal.relay, selfPublicKey, peerPublicKey, pairingProposal.permissions, controllerPublicKey, expiry)
        val preSettlementPairingApprove = pairingProposal.toApprove(1, settledSequence.settledTopic, expiry)

        relayRepository.events
            .filter { it is WebSocket.Event.OnConnectionOpened<*> }
            .collect {
                relayRepository.publish(pairingProposal.topic, preSettlementPairingApprove)
            }
    }

    private fun settle(
        relay: JSONObject,
        selfPublicKey: PublicKey,
        peerPublicKey: PublicKey,
        permissions: PairingProposedPermissions?,
        controllerPublicKey: PublicKey,
        expiry: Expiry
    ): SettledSequence {
        val settledTopic = crypto.generateSharedKey(selfPublicKey, peerPublicKey)

        return SettledSequence(settledTopic, relay, selfPublicKey, peerPublicKey, permissions to controllerPublicKey, expiry)
    }

    private data class SettledSequence(
        val settledTopic: Topic,
        val relay: JSONObject,
        val selfPublicKey: PublicKey,
        val peerPublicKey: PublicKey,
        val sequencePermissions: Pair<PairingProposedPermissions?, PublicKey>,
        val expiry: Expiry
    )
}