package com.walletconnect.chat.authentication.jwt

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


sealed interface JwtClaims {
    @JsonClass(generateAdapter = true)
    data class InviteKey(
        @Json(name = "iss") val issuer: String,
        @Json(name = "sub") val subject: String,
        @Json(name = "aud") val audience: String,
        @Json(name = "iat") val issuedAt: Long,
        @Json(name = "exp") val expiration: Long,
        @Json(name = "pkh") val pkh: String,
    ) : JwtClaims
}