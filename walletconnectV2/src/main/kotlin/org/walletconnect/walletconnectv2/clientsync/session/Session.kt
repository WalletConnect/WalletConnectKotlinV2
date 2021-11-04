package org.walletconnect.walletconnectv2.clientsync.session

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.walletconnect.walletconnectv2.clientsync.session.after.params.Request
import org.walletconnect.walletconnectv2.clientsync.session.before.proposal.RelayProtocolOptions
import org.walletconnect.walletconnectv2.clientsync.session.before.proposal.SessionProposedPermissions
import org.walletconnect.walletconnectv2.clientsync.session.before.proposal.SessionProposer
import org.walletconnect.walletconnectv2.clientsync.session.before.proposal.SessionSignal
import org.walletconnect.walletconnectv2.clientsync.session.before.success.SessionParticipant
import org.walletconnect.walletconnectv2.clientsync.session.before.success.SessionState
import org.walletconnect.walletconnectv2.common.Expiry
import org.walletconnect.walletconnectv2.common.Topic
import org.walletconnect.walletconnectv2.common.Ttl
import org.walletconnect.walletconnectv2.common.network.adapters.ExpiryAdapter
import org.walletconnect.walletconnectv2.common.network.adapters.TopicAdapter
import org.walletconnect.walletconnectv2.common.network.adapters.TtlAdapter

sealed class Session {

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

    class Failure(val reason: String) : Session()

    data class RequestParams(
        @Json(name = "request")
        val request: Request,
        @Json(name = "chainId")
        val chainId: String?
    ) : Session()
}