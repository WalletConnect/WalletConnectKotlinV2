package org.walletconnect.walletconnectv2.outofband.pairing

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.json.JSONObject
import org.walletconnect.walletconnectv2.common.Expiry
import org.walletconnect.walletconnectv2.common.Topic
import org.walletconnect.walletconnectv2.common.Ttl
import org.walletconnect.walletconnectv2.outofband.pairing.proposal.PairingProposedPermissions
import org.walletconnect.walletconnectv2.outofband.pairing.proposal.PairingProposer
import org.walletconnect.walletconnectv2.outofband.pairing.proposal.PairingSignal
import org.walletconnect.walletconnectv2.outofband.pairing.success.PairingParticipant
import org.walletconnect.walletconnectv2.outofband.pairing.success.PairingState

sealed class Pairing {

    data class Proposal(
        val topic: Topic,
        val relay: JSONObject,
        val pairingProposer: PairingProposer,
        val pairingSignal: PairingSignal?,
        val permissions: PairingProposedPermissions?,
        val ttl: Ttl
    ): Pairing()

    @JsonClass(generateAdapter = true)
    data class Success(
        @Json(name = "topic")
        val topic: Topic,
        @Json(name = "relay")
        val relay: JSONObject,
        @Json(name = "responder")
        val responder: PairingParticipant,
        @Json(name = "expiry")
        val expiry: Expiry,
        @Json(name = "state")
        val state: PairingState
    ): Pairing()

    class Failure(val reason: String): Pairing()
}