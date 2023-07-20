package com.walletconnect.push.data.wellknown.did


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DidJsonDTO(
    @Json(name = "@context")
    val context: List<String>,
    @Json(name = "id")
    val id: String,
    @Json(name = "verificationMethod")
    val verificationMethod: List<VerificationMethodDTO>,
    @Json(name = "keyAgreement")
    val keyAgreement: List<String>
)