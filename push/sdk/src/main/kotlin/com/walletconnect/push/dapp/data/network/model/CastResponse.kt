package com.walletconnect.push.dapp.data.network.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

sealed class CastResponse {

    @JsonClass(generateAdapter = true)
    data class Notify(
        @Json(name = "sent")
        val sent: List<String>,
        @Json(name = "failed")
        val failed: List<Failed>,
        @Json(name = "notFound")
        val notFound: List<String>
    ) {

        @JsonClass(generateAdapter = true)
        data class Failed(
            @Json(name = "account")
            val account: String,
            @Json(name = "reason")
            val reason: String
        )
    }
}