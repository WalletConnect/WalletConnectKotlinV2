package com.walletconnect.push.dapp.network.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

sealed class CastResponseDTO {

    @JsonClass(generateAdapter = true)
    data class Notify(
        @Json(name = "sent")
        val sent: List<String>,
        @Json(name = "failed")
        val failed: List<List<String>>,
        @Json(name = "notFound")
        val notFound: List<String>
    )
}
