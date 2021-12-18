package com.walletconnect.walletconnectv2.clientsync.session

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.walletconnectv2.ClientParams
import com.walletconnect.walletconnectv2.clientsync.session.after.params.Reason
import com.walletconnect.walletconnectv2.clientsync.session.after.params.SessionPermissions
import com.walletconnect.walletconnectv2.clientsync.session.after.params.SessionRequest
import com.walletconnect.walletconnectv2.clientsync.session.before.proposal.RelayProtocolOptions
import com.walletconnect.walletconnectv2.clientsync.session.before.proposal.SessionProposedPermissions
import com.walletconnect.walletconnectv2.clientsync.session.before.proposal.SessionProposer
import com.walletconnect.walletconnectv2.clientsync.session.before.proposal.SessionSignal
import com.walletconnect.walletconnectv2.clientsync.session.before.success.SessionParticipant
import com.walletconnect.walletconnectv2.clientsync.session.common.SessionState
import com.walletconnect.walletconnectv2.common.Expiry
import com.walletconnect.walletconnectv2.common.Topic
import com.walletconnect.walletconnectv2.common.Ttl
import com.walletconnect.walletconnectv2.common.network.adapters.ExpiryAdapter
import com.walletconnect.walletconnectv2.common.network.adapters.TopicAdapter
import com.walletconnect.walletconnectv2.common.network.adapters.TtlAdapter

sealed class Session: ClientParams {

    @JsonClass(generateAdapter = true)
    data class Proposal(
        @Json(name = "topic")
        @field:TopicAdapter.Qualifier
        val topic: Topic,
        @Json(name = "relay")
        val relay: RelayProtocolOptions,
        @Json(name = "proposer")
        val proposer: SessionProposer,
        @Json(name = "signal")
        val signal: SessionSignal,
        @Json(name = "permissions")
        val permissions: SessionProposedPermissions,
        @Json(name = "ttl")
        @field:TtlAdapter.Qualifier
        val ttl: Ttl
    ) : Session()

    @JsonClass(generateAdapter = true)
    data class Success(
        @Json(name = "relay")
        val relay: RelayProtocolOptions,
        @Json(name = "responder")
        val responder: SessionParticipant,
        @Json(name = "expiry")
        @ExpiryAdapter.Qualifier
        val expiry: Expiry,
        @Json(name = "state")
        val state: SessionState
    ) : Session()

    @JsonClass(generateAdapter = true)
    class Failure(val reason: String) : Session()

    @JsonClass(generateAdapter = true)
    data class SessionPayloadParams(
        @Json(name = "request")
        val request: SessionRequest,
        @Json(name = "chainId")
        val chainId: String?
    ) : Session()

    @JsonClass(generateAdapter = true)
    class DeleteParams(
        @Json(name = "reason")
        val reason: Reason
    ) : Session()

    class UpdateParams(
        @Json(name = "state")
        val state: SessionState
    ) : Session()

    data class SessionPermissionsParams(
        @Json(name = "permissions")
        val permissions: SessionPermissions
    ) : Session()

    class PingParams : Session()

    data class NotificationParams(
        @Json(name = "type")
        val type: String,
        @Json(name = "data")
        val data: Any
    ) : Session()
}