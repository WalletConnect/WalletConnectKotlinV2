package org.walletconnect.walletconnectv2.data.domain.pairing

import org.json.JSONObject
import org.walletconnect.walletconnectv2.data.domain.pairing.proposal.PairingProposedPermissions
import org.walletconnect.walletconnectv2.data.domain.pairing.proposal.PairingProposer
import org.walletconnect.walletconnectv2.data.domain.pairing.proposal.PairingSignal
import org.walletconnect.walletconnectv2.data.domain.pairing.success.PairingParticipant
import org.walletconnect.walletconnectv2.data.domain.pairing.success.PairingState
import java.net.URI
import java.util.*

sealed class Pairing {

    data class Proposal(
        val topic: String,
        val relay: JSONObject,
        val pairingProposer: PairingProposer,
        val pairingSignal: PairingSignal?,
        val permissions: PairingProposedPermissions?,
        val ttl: Long
    ): Pairing()

    data class Success(
        val id: Int,
        val jsonrpc: String,
        val method: String,
        val topic: String,
        val relay: JSONObject,
        val responder: PairingParticipant,
        val expiry: Long,
        val state: PairingState
    ): Pairing()

    class Failure(val reason: String): Pairing()

    companion object {

        @JvmStatic
        internal fun String.toPairProposal(): Proposal {
            val properUriString = if (contains("wc://")) this else replace("wc:", "wc://")
            val pairUri = URI(properUriString)
            val mapOfQueryParameters: Map<String, String> = pairUri.query.split("&").associate { it.substringBefore("=") to it.substringAfter("=") }
            val relay = JSONObject(mapOfQueryParameters["relay"] ?: "{}")
            val publicKey = mapOfQueryParameters["publicKey"] ?: ""
            val controller: Boolean = mapOfQueryParameters["controller"].toBoolean()
            val ttl: Long = Calendar.getInstance().also { it.add(Calendar.DAY_OF_YEAR, 30) }.timeInMillis

            return Proposal(
                topic = pairUri.userInfo,
                relay = relay,
                pairingProposer = PairingProposer(publicKey, controller),
                pairingSignal = null,
                permissions = null,
                ttl = ttl
            )
        }

        @JvmStatic
        internal fun Proposal.generateSuccessResponse() {

        }
    }
}
