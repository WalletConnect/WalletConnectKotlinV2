package com.walletconnect.walletconnectv2.relay.model.clientsync.session.after.params

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SessionRequest(
    @Json(name = "method")
    val method: String,
    @Json(name = "params")
    val params: Any
)