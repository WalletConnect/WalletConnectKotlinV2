@file:JvmSynthetic

package com.walletconnect.auth.common.json_rpc.payload

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

//todo: Discuss: Example DTO
@JsonClass(generateAdapter = true)
internal data class PayloadParamsDTO(
    @Json(name = "type")
    val type: String,
    @Json(name = "chainId")
    val chainId: String,
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