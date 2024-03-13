package com.walletconnect.android.pulse.model.properties

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ModalConnectedProperties(
    @Json(name = "connected")
    val connected: Boolean
)