package org.walletconnect.walletconnectv2.clientsync.session.after.params

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Request(
    @Json(name = "method")
    val method: String,
    @Json(name = "params")
    val params: Any
)