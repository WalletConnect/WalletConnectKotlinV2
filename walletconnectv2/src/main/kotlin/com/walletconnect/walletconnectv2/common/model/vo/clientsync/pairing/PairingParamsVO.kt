package com.walletconnect.walletconnectv2.common.model.vo.clientsync.pairing

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.json.JSONObject
import com.walletconnect.walletconnectv2.common.model.type.ClientParams
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.pairing.after.payload.ProposalRequestVO
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.pairing.before.proposal.PairingProposedPermissionsVO
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.pairing.before.proposal.PairingProposerVO
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.pairing.before.proposal.PairingSignalVO
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.pairing.before.success.PairingParticipantVO
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.pairing.before.success.PairingStateVO
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.session.after.params.ReasonVO
import com.walletconnect.walletconnectv2.common.model.vo.ExpiryVO
import com.walletconnect.walletconnectv2.common.model.vo.TopicVO
import com.walletconnect.walletconnectv2.common.model.vo.TtlVO
import com.walletconnect.walletconnectv2.common.adapters.ExpiryAdapter
import com.walletconnect.walletconnectv2.common.adapters.JSONObjectAdapter
import com.walletconnect.walletconnectv2.common.adapters.TopicAdapter
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.session.before.proposal.RelayProtocolOptionsVO

internal sealed class PairingParamsVO : ClientParams {

    internal data class Proposal(
        val topic: TopicVO,
        val relay: RelayProtocolOptionsVO,
        val proposer: PairingProposerVO,
        val signal: PairingSignalVO?,
        val permissions: PairingProposedPermissionsVO?,
        val ttl: TtlVO
    ) : PairingParamsVO()

    @JsonClass(generateAdapter = true)
    internal data class ApproveParams(
        @Json(name = "topic")
        @TopicAdapter.Qualifier
        val settledTopic: TopicVO,
        @Json(name = "relay")
        val relay: RelayProtocolOptionsVO,
        @Json(name = "responder")
        val responder: PairingParticipantVO,
        @Json(name = "expiry")
        @ExpiryAdapter.Qualifier
        val expiry: ExpiryVO,
        @Json(name = "state")
        val state: PairingStateVO
    ) : PairingParamsVO()

    class RejectParams(val reason: String) : PairingParamsVO()

    @JsonClass(generateAdapter = true)
    internal data class PayloadParams(
        @Json(name = "request")
        val request: ProposalRequestVO
    ) : PairingParamsVO()

    @JsonClass(generateAdapter = true)
    internal class DeleteParams(
        @Json(name = "reason")
        val reason: ReasonVO
    ) : PairingParamsVO()

    @Suppress("CanSealedSubClassBeObject")
    internal class PingParams : PairingParamsVO()

    internal data class NotificationParams(
        @Json(name = "type")
        val type: String,
        @Json(name = "data")
        val data: Any
    ) : PairingParamsVO()

    @JsonClass(generateAdapter = true)
    internal data class UpdateParams(
        @Json(name = "state")
        val state: PairingStateVO
    ) : PairingParamsVO()
}