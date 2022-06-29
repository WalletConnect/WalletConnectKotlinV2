package com.walletconnect.sign.crypto.data.repository.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class IridiumJWTPayload(@Json(name = "iss") val issuer: String, @Json(name = "sub") val subject: String)