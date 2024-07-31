package com.walletconnect.android.verify.client

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VerifyServerPublicKey(
    @Json(name = "publicKey")
    val publicKey: String,
    @Json(name = "expiresAt")
    val expiresAt: String,
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
