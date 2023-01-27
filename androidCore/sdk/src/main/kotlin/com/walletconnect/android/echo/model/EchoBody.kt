package com.walletconnect.android.echo.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EchoBody(
    @Json(name = "client_id")
    val clientId: String,
    @Json(name = "token")
    val token: String,
    @Json(name = "type")
    val type: String = "fcm"
)