package com.walletconnect.walletconnectv2.relay.model.clientsync.pairing

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.json.JSONObject
import com.walletconnect.walletconnectv2.relay.model.clientsync.types.ClientParams
import com.walletconnect.walletconnectv2.relay.model.clientsync.pairing.after.payload.ProposalRequest
import com.walletconnect.walletconnectv2.relay.model.clientsync.pairing.before.proposal.PairingProposedPermissions
import com.walletconnect.walletconnectv2.relay.model.clientsync.pairing.before.proposal.PairingProposer
import com.walletconnect.walletconnectv2.relay.model.clientsync.pairing.before.proposal.PairingSignal
import com.walletconnect.walletconnectv2.relay.model.clientsync.pairing.before.success.PairingParticipant
import com.walletconnect.walletconnectv2.relay.model.clientsync.pairing.before.success.PairingState
import com.walletconnect.walletconnectv2.relay.model.clientsync.session.after.params.Reason
import com.walletconnect.walletconnectv2.common.model.vo.ExpiryVO
import com.walletconnect.walletconnectv2.common.model.vo.TopicVO
import com.walletconnect.walletconnectv2.common.model.vo.TtlVO
import com.walletconnect.walletconnectv2.common.adapters.ExpiryAdapter
import com.walletconnect.walletconnectv2.common.adapters.JSONObjectAdapter
import com.walletconnect.walletconnectv2.common.adapters.TopicAdapter

internal sealed class Pairing : ClientParams {

    data class Proposal(
        val topic: TopicVO,
        val relay: JSONObject,
        val pairingProposer: PairingProposer,
        val pairingSignal: PairingSignal?,
        val permissions: PairingProposedPermissions?,
        val ttl: TtlVO
    ) : Pairing()

    @JsonClass(generateAdapter = true)
    data class Success(
        @Json(name = "topic")
        @TopicAdapter.Qualifier
        val settledTopic: TopicVO,
        @Json(name = "relay")
        @JSONObjectAdapter.Qualifier
        val relay: JSONObject,
        @Json(name = "responder")
        val responder: PairingParticipant,
        @Json(name = "expiry")
        @ExpiryAdapter.Qualifier
        val expiry: ExpiryVO,
        @Json(name = "state")
        val state: PairingState
    ) : Pairing()

    class Failure(val reason: String) : Pairing()

    @JsonClass(generateAdapter = true)
    data class PayloadParams(
        @Json(name = "request")
        val request: ProposalRequest
    ) : Pairing()

    @JsonClass(generateAdapter = true)
    class DeleteParams(
        @Json(name = "reason")
        val reason: Reason
    ) : Pairing()

    class PingParams : Pairing()

    data class NotificationParams(
        @Json(name = "type")
        val type: String,
        @Json(name = "data")
        val data: Any
    ) : Pairing()

    @JsonClass(generateAdapter = true)
    data class UpdateParams(
        @Json(name = "state")
        val state: PairingState
    ) : Pairing()
}