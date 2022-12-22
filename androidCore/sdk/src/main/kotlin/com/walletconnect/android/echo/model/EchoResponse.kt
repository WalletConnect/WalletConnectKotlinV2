package com.walletconnect.android.echo.model


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EchoResponse(
    @Json(name = "errors")
    val errors: List<Error>,
    @Json(name = "fields")
    val fields: List<Field>,
    @Json(name = "status")
    val status: String
) {
    @JsonClass(generateAdapter = true)
    data class Error(
        @Json(name = "message")
        val message: String,
        @Json(name = "name")
        val name: String
    )

    @JsonClass(generateAdapter = true)
    data class Field(
        @Json(name = "description")
        val description: String,
        @Json(name = "field")
        val `field`: String,
        @Json(name = "location")
        val location: String
    )
}