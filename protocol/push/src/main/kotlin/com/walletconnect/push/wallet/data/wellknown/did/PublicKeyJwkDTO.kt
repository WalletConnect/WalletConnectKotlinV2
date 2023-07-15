package com.walletconnect.push.wallet.data.wellknown.did


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PublicKeyJwkDTO(
    @Json(name = "kty")
    val kty: String,
    @Json(name = "crv")
    val crv: String,
    @Json(name = "x")
    val x: String
)