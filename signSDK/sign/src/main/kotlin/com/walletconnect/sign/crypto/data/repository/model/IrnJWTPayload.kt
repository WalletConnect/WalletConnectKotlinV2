package com.walletconnect.sign.crypto.data.repository.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class IrnJWTPayload(
    @Json(name = "iss") val issuer: String,
    @Json(name = "sub") val subject: String,
    @Json(name = "aud") val audience: String,
    @Json(name = "iat") val issuedAt: Long,
    @Json(name = "exp") val expiration: Long,
)