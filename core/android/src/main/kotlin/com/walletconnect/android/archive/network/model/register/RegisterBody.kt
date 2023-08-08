package com.walletconnect.android.archive.network.model.register

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RegisterBody(
    @Json(name = "tags")
    val tags: List<String>,
    @Json(name = "relayUrl")
    val relayUrl: String,
)
