package com.walletconnect.push.common.data.jwt

import com.walletconnect.foundation.util.jwt.JwtClaims
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PushSubscriptionJwtClaim(
    @Json(name = "iss") override val issuer: String,
    @Json(name = "sub") val subject: String,
    @Json(name = "aud") val audience: String,
    @Json(name = "iat") val issuedAt: Long,
    @Json(name = "exp") val expiration: Long,
    @Json(name = "ksu") val keyserverUrl: String,
) : JwtClaims