package com.walletconnect.android.pulse.model.properties

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ConnectSuccessProperties(
    @Json(name = "name")
    val name: String,
    @Json(name = "platform")
    val platform: String
)