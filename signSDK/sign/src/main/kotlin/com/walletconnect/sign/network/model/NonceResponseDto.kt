package com.walletconnect.sign.network.model


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NonceResponseDto(
    @Json(name = "nonce")
    val nonce: String
)