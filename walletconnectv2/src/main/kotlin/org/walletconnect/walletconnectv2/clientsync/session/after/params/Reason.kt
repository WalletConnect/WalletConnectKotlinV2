package org.walletconnect.walletconnectv2.clientsync.session.after.params

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Reason(
    @Json(name = "code")
    val code: Int = -1, //TODO add default error code
    @Json(name = "message")
    val message: String
)