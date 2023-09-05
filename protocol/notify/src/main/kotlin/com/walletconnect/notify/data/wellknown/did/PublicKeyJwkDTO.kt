@file:JvmSynthetic

package com.walletconnect.notify.data.wellknown.did

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class PublicKeyJwkDTO(
    @Json(name = "kty")
    val kty: String,
    @Json(name = "crv")
    val crv: String,
    @Json(name = "x")
    val x: String
)