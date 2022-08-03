package com.walletconnect.sign.crypto.data.repository.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class IrnJWTHeader(@Json(name = "alg") val algorithm: String, @Json(name = "typ") val type: String)

