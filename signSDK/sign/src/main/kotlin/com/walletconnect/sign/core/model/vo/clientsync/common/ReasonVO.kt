package com.walletconnect.sign.core.model.vo.clientsync.common

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.sign.util.DefaultId

@JsonClass(generateAdapter = true)
internal data class ReasonVO(
    @Json(name = "code")
    val code: Int = Int.DefaultId,
    @Json(name = "message")
    val message: String
)