package org.walletconnect.walletconnectv2.clientsync.pairing

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.json.JSONObject
import org.walletconnect.walletconnectv2.common.Expiry
import org.walletconnect.walletconnectv2.common.Topic
import org.walletconnect.walletconnectv2.common.Ttl
import org.walletconnect.walletconnectv2.common.network.adapters.ExpiryAdapter
import org.walletconnect.walletconnectv2.common.network.adapters.JSONObjectAdapter
import org.walletconnect.walletconnectv2.common.network.adapters.TopicAdapter
import org.walletconnect.walletconnectv2.clientsync.pairing.proposal.PairingProposedPermissions
import org.walletconnect.walletconnectv2.clientsync.pairing.proposal.PairingProposer
import org.walletconnect.walletconnectv2.clientsync.pairing.proposal.PairingSignal
import org.walletconnect.walletconnectv2.clientsync.pairing.success.PairingParticipant
import org.walletconnect.walletconnectv2.clientsync.pairing.success.PairingState

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
        @TopicAdapter.Qualifier
        val settledTopic: Topic,
        @Json(name = "relay")
        @JSONObjectAdapter.Qualifier
        val relay: JSONObject,
        @Json(name = "responder")
        val responder: PairingParticipant,
        @Json(name = "expiry")
        @ExpiryAdapter.Qualifier
        val expiry: Expiry,
        @Json(name = "state")
        val state: PairingState
    ): Pairing()

    class Failure(val reason: String): Pairing()
}