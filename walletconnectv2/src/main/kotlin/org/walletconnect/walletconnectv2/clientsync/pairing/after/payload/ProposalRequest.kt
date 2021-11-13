package org.walletconnect.walletconnectv2.clientsync.pairing.after.payload

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.walletconnect.walletconnectv2.clientsync.session.Session

@JsonClass(generateAdapter = true)
data class ProposalRequest(
    @Json(name = "method")
    val method: String,
    @Json(name = "params")
    val params: Session.Proposal
)