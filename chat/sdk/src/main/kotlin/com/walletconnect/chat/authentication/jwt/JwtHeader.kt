@file:JvmSynthetic

package com.walletconnect.chat.authentication.jwt

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
internal class JwtHeader(
    @Json(name = "alg")
    val algorithm: String,
    @Json(name = "typ")
    val type: String,
) {
    @Json(ignore = true)
    val encoded = encodeJSON(this) // encode on init to not encode every time jwt is generated

    companion object {
        val EdDSA = JwtHeader("EdDSA", "JWT")
    }
}