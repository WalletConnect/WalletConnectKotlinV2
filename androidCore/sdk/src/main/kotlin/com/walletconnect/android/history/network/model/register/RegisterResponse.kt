package com.walletconnect.android.history.network.model.register

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.android.echo.network.model.EchoResponse


@JsonClass(generateAdapter = true)
data class RegisterResponse(
    @Json(name = "errors")
    val errors: List<EchoResponse.Error>?,
    @Json(name = "fields")
    val fields: List<EchoResponse.Field>?,
    @Json(name = "status")
    val status: String
)
