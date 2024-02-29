package com.walletconnect.android.pulse

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PulseResponse(
    @Json(name = "errors")
    val errors: List<Error>?,
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
}
