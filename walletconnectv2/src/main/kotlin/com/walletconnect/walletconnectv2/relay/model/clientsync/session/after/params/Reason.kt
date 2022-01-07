package com.walletconnect.walletconnectv2.relay.model.clientsync.session.after.params

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.walletconnectv2.util.DefaultId

@JsonClass(generateAdapter = true)
internal data class Reason(
    @Json(name = "code")
    val code: Int = Int.DefaultId,
    @Json(name = "message")
    val message: String
)