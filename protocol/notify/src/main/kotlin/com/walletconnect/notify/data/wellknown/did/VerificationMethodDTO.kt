package com.walletconnect.notify.data.wellknown.did


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.notify.data.wellknown.did.PublicKeyJwkDTO

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