package com.walletconnect.sign.common.model.vo.clientsync.common

import com.squareup.moshi.Json

data class Caip222Request(
    @Json(name = "type")
    val type: String,
    @Json(name = "chains")
    val chains: List<String>,
    @Json(name = "domain")
    val domain: String,
    @Json(name = "aud")
    val aud: String,
    @Json(name = "version")
    val version: String,
    @Json(name = "nonce")
    val nonce: String,
    @Json(name = "iat")
    val iat: String,
    @Json(name = "nbf")
    val nbf: String?,
    @Json(name = "exp")
    val exp: String?,
    @Json(name = "statement")
    val statement: String?,
    @Json(name = "requestId")
    val requestId: String?,
    @Json(name = "resources")
    val resources: List<String>?,
)