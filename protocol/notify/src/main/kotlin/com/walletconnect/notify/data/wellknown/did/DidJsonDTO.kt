@file:JvmSynthetic

package com.walletconnect.notify.data.wellknown.did

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class DidJsonDTO(
    @Json(name = "@context")
    val context: List<String>,
    @Json(name = "id")
    val id: String,
    @Json(name = "verificationMethod")
    val verificationMethod: List<VerificationMethodDTO>,
    @Json(name = "keyAgreement")
    val keyAgreement: List<String>,
    @Json(name = "authentication")
    val authentication: List<String>
)