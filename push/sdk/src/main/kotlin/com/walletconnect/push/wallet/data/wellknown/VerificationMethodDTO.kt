package com.walletconnect.push.wallet.data.wellknown


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VerificationMethodDTO(
    @Json(name = "id")
    val id: String,
    @Json(name = "type")
    val type: String,
    @Json(name = "controller")
    val controller: String,
    @Json(name = "publicKeyJwk")
    val publicKeyJwk: PublicKeyJwkDTO
)