package com.walletconnect.chat.core.model.vo.clientsync.params

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MediaVO(
    @Json(name = "type")
    val type: String,
    @Json(name = "data")
    val data: String
)