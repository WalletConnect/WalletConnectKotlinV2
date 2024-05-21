package com.walletconnect.android.pulse.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.android.internal.utils.currentTimeInSeconds
import com.walletconnect.util.generateId

@JsonClass(generateAdapter = true)
data class Event<T>(
    @Json(name = "eventId")
    val eventId: Long = generateId(),
    @Json(name = "bundleId")
    val bundleId: String,
    @Json(name = "timestamp")
    val timestamp: Long = currentTimeInSeconds,
    @Json(name = "props")
    val props: T
)