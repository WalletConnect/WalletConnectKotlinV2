package com.walletconnect.sign.core.model.vo.clientsync.session.payload

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class EventsVO(
    @Json(name = "names")
    val names: List<String>,
)