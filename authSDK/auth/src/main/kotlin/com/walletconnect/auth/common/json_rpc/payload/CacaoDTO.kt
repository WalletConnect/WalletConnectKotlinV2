@file:JvmSynthetic

package com.walletconnect.auth.common.json_rpc.payload

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

//todo: Discuss: Example DTO
//todo: Engine do dla cacao
@JsonClass(generateAdapter = true)
internal data class CacaoDTO(
    @Json(name = "header")
    val header: HeaderDTO,
    @Json(name = "payload")
    val payload: PayloadDTO,
    @Json(name = "signature")
    val signature: SignatureDTO,
) {
    @JsonClass(generateAdapter = true)
    data class SignatureDTO(
        @Json(name = "t")
        val t: String,
        @Json(name = "s")
        val s: String,
        @Json(name = "m")
        val m: String? = null,
    )

    @JsonClass(generateAdapter = true)
    data class HeaderDTO(
        @Json(name = "t")
        val t: String,
    )

    @JsonClass(generateAdapter = true)
    data class PayloadDTO(
        @Json(name = "iss")
        val iss: String,
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
}