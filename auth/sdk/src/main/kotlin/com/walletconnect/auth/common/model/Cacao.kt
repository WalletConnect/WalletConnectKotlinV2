@file:JvmSynthetic

package com.walletconnect.auth.common.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

//@JsonClass(generateAdapter = true)
//internal data class Cacao(
//    @Json(name = "header")
//    val header: Header,
//    @Json(name = "payload")
//    val payload: Payload,
//    @Json(name = "signature")
//    val signature: Signature,
//) {
//    @JsonClass(generateAdapter = true)
//    data class Signature(
//        @Json(name = "t")
//        val t: String,
//        @Json(name = "s")
//        val s: String,
//        @Json(name = "m")
//        val m: String? = null,
//    )
//
//    @JsonClass(generateAdapter = true)
//    data class Header(
//        @Json(name = "t")
//        val t: String,
//    )
//
//    @JsonClass(generateAdapter = true)
//    data class Payload(
//        @Json(name = "iss")
//        val iss: String,
//        @Json(name = "domain")
//        val domain: String,
//        @Json(name = "aud")
//        val aud: String,
//        @Json(name = "version")
//        val version: String,
//        @Json(name = "nonce")
//        val nonce: String,
//        @Json(name = "iat")
//        val iat: String,
//        @Json(name = "nbf")
//        val nbf: String?,
//        @Json(name = "exp")
//        val exp: String?,
//        @Json(name = "statement")
//        val statement: String?,
//        @Json(name = "requestId")
//        val requestId: String?,
//        @Json(name = "resources")
//        val resources: List<String>?,
//    ) {
//        val issuer = Issuer(iss)
//    }
//}
