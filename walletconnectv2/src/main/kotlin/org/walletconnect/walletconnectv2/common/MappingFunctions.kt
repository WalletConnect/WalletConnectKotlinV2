@file:JvmName("MappingFunctions")

package org.walletconnect.walletconnectv2.common

import com.squareup.moshi.Moshi
import org.json.JSONObject
import org.walletconnect.walletconnectv2.clientsync.PreSettlementPairing
import org.walletconnect.walletconnectv2.clientsync.pairing.Pairing
import org.walletconnect.walletconnectv2.clientsync.pairing.proposal.PairingProposer
import org.walletconnect.walletconnectv2.clientsync.pairing.success.PairingParticipant
import org.walletconnect.walletconnectv2.clientsync.pairing.success.PairingState
import org.walletconnect.walletconnectv2.crypto.data.PublicKey
import org.walletconnect.walletconnectv2.relay.data.model.Relay
import java.net.URI
import kotlin.time.Duration

internal fun String.toPairProposal(): Pairing.Proposal {
    val properUriString = if (contains("wc://")) this else replace("wc:", "wc://")
    val pairUri = URI(properUriString)
    val mapOfQueryParameters: Map<String, String> =
        pairUri.query.split("&")
            .associate { query -> query.substringBefore("=") to query.substringAfter("=") }
    val relay = JSONObject(mapOfQueryParameters["relay"] ?: "{}")
    val publicKey = mapOfQueryParameters["publicKey"] ?: ""
    val controller: Boolean = mapOfQueryParameters["controller"].toBoolean()
    val ttl: Long = Duration.days(30).inWholeSeconds

    return Pairing.Proposal(
        topic = Topic(pairUri.userInfo),
        relay = relay,
        pairingProposer = PairingProposer(publicKey, controller),
        pairingSignal = null,
        permissions = null,
        ttl = Ttl(ttl)
    )
}

internal fun Pairing.Proposal.toPairingSuccess(
    settleTopic: Topic,
    expiry: Expiry,
    selfPublicKey: PublicKey
): Pairing.Success {
    return Pairing.Success(
        settledTopic = settleTopic,
        relay = relay,
        responder = PairingParticipant(publicKey = selfPublicKey.keyAsHex),
        expiry = expiry,
        state = PairingState(null)
    )
}

internal fun Pairing.Proposal.toApprove(
    id: Long,
    settleTopic: Topic,
    expiry: Expiry,
    selfPublicKey: PublicKey
): PreSettlementPairing.Approve {
    return PreSettlementPairing.Approve(
        id = id,
        params = this.toPairingSuccess(settleTopic, expiry, selfPublicKey)
    )
}

internal fun PreSettlementPairing.Approve.toRelayPublishRequest(
    id: Long,
    topic: Topic,
    moshi: Moshi
): Relay.Publish.Request {
    val pairingApproveJson = moshi.adapter(PreSettlementPairing.Approve::class.java).toJson(this)
    val hexEncodedJson = pairingApproveJson.encodeToByteArray().joinToString(separator = "") {
        String.format("%02X", it)
    }

    return Relay.Publish.Request(
        id = id,
        params = Relay.Publish.Request.Params(topic = topic, message = hexEncodedJson.lowercase())
    )
}