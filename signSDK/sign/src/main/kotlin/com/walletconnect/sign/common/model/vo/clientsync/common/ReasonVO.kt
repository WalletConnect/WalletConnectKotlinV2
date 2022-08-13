package com.walletconnect.sign.common.model.vo.clientsync.common

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.utils.DefaultId

@JsonClass(generateAdapter = true)
internal data class ReasonVO(
    @Json(name = "code")
    val code: Int = Int.DefaultId,
    @Json(name = "message")
    val message: String
)