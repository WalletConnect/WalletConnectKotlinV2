package com.walletconnect.android.pulse.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.android.pulse.model.properties.Props

@JsonClass(generateAdapter = true)
data class Event(
    @Json(name = "eventId")
    val eventId: Long,
    @Json(name = "bundleId")
    val bundleId: String,
    @Json(name = "timestamp")
    val timestamp: Long,
    @Json(name = "props")
    val props: Props
)