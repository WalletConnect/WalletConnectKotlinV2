package com.walletconnect.android.verify.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VerifyClaims(
    @Json(name = "origin") val origin: String,
    @Json(name = "id") val id: String,
    @Json(name = "isScam") val isScam: String?,
    @Json(name = "exp") val expiration: Long
)
