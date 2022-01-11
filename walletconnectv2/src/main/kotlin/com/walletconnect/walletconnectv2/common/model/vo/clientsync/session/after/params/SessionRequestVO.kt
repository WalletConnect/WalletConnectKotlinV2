package com.walletconnect.walletconnectv2.common.model.vo.clientsync.session.after.params

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class SessionRequestVO(
    @Json(name = "method")
    val method: String,
    @Json(name = "params")
    val params: Any
)