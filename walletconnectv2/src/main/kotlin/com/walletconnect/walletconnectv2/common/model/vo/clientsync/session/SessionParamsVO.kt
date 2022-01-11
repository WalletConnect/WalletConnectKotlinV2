package com.walletconnect.walletconnectv2.common.model.vo.clientsync.session

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.walletconnectv2.common.model.type.ClientParams
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.session.after.params.ReasonVO
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.session.after.params.SessionPermissionsVO
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.session.after.params.SessionRequestVO
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.session.before.proposal.RelayProtocolOptionsVO
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.session.before.proposal.SessionProposedPermissionsVO
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.session.before.proposal.SessionProposerVO
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.session.before.proposal.SessionSignalVO
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.session.before.success.SessionParticipantVO
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.session.common.SessionStateVO
import com.walletconnect.walletconnectv2.common.model.vo.ExpiryVO
import com.walletconnect.walletconnectv2.common.model.vo.TopicVO
import com.walletconnect.walletconnectv2.common.model.vo.TtlVO
import com.walletconnect.walletconnectv2.common.adapters.ExpiryAdapter
import com.walletconnect.walletconnectv2.common.adapters.TopicAdapter
import com.walletconnect.walletconnectv2.common.adapters.TtlAdapter

internal sealed class SessionParamsVO: ClientParams {

    @JsonClass(generateAdapter = true)
    data class Proposal(
        @Json(name = "topic")
        @field:TopicAdapter.Qualifier
        val topic: TopicVO,
        @Json(name = "relay")
        val relay: RelayProtocolOptionsVO,
        @Json(name = "proposer")
        val proposer: SessionProposerVO,
        @Json(name = "signal")
        val signal: SessionSignalVO,
        @Json(name = "permissions")
        val permissions: SessionProposedPermissionsVO,
        @Json(name = "ttl")
        @field:TtlAdapter.Qualifier
        val ttl: TtlVO
    ) : SessionParamsVO()

    @JsonClass(generateAdapter = true)
    data class Success(
        @Json(name = "relay")
        val relay: RelayProtocolOptionsVO,
        @Json(name = "responder")
        val responder: SessionParticipantVO,
        @Json(name = "expiry")
        @ExpiryAdapter.Qualifier
        val expiry: ExpiryVO,
        @Json(name = "state")
        val state: SessionStateVO
    ) : SessionParamsVO()

    @JsonClass(generateAdapter = true)
    class Failure(val reason: String) : SessionParamsVO()

    @JsonClass(generateAdapter = true)
    data class SessionPayloadParams(
        @Json(name = "request")
        val request: SessionRequestVO,
        @Json(name = "chainId")
        val chainId: String?
    ) : SessionParamsVO()

    @JsonClass(generateAdapter = true)
    class DeleteParams(
        @Json(name = "reason")
        val reason: ReasonVO
    ) : SessionParamsVO()

    class UpdateParams(
        @Json(name = "state")
        val state: SessionStateVO
    ) : SessionParamsVO()

    data class SessionPermissionsParams(
        @Json(name = "permissions")
        val permissions: SessionPermissionsVO
    ) : SessionParamsVO()

    class PingParams : SessionParamsVO()

    data class NotificationParams(
        @Json(name = "type")
        val type: String,
        @Json(name = "data")
        val data: Any
    ) : SessionParamsVO()
}