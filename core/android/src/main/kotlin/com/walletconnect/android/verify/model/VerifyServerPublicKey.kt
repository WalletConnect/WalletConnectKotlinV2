package com.walletconnect.android.verify.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VerifyServerPublicKey(
    @Json(name = "publicKey")
    val jwk: JWK,
    @Json(name = "expiresAt")
    val expiresAt: Long,
)

@JsonClass(generateAdapter = true)
data class JWK(
    @Json(name = "crv")
    val crv: String,
    @Json(name = "ext")
    val ext: Boolean,
    @Json(name = "key_ops")
    val keyOps: List<String>,
    @Json(name = "kty")
    val kty: String,
    @Json(name = "x")
    val x: String,
    @Json(name = "y")
    val y: String,
)
