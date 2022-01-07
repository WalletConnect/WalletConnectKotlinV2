package com.walletconnect.walletconnectv2.relay.model.clientsync.pairing.after.payload

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.walletconnectv2.relay.model.clientsync.session.Session

@JsonClass(generateAdapter = true)
data class ProposalRequest(
    @Json(name = "method")
    val method: String,
    @Json(name = "params")
    val params: Session.Proposal
)