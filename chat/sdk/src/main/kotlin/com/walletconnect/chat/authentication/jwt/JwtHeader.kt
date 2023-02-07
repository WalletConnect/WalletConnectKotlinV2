package com.walletconnect.chat.authentication.jwt

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
class JwtHeader(
    @Json(name = "alg")
    val algorithm: String,
    @Json(name = "typ")
    val type: String,
) {
    companion object {
        val EdDSA = JwtHeader("EdDSA", "JWT")
    }
}