package com.walletconnect.push.dapp.data.network.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

sealed class CastBody {

    @JsonClass(generateAdapter = true)
    data class Register(
        @Json(name = "account")
        val account: String,
        @Json(name = "symKey")
        val symKey: String,
        @Json(name = "relayUrl")
        val relayUrl: String
    ): CastBody()

    @JsonClass(generateAdapter = true)
    data class Notify(
        @Json(name = "notification")
        val notification: Notification,
        @Json(name = "accounts")
        val accounts: List<String>
    ) {
        @JsonClass(generateAdapter = true)
        data class Notification(
            @Json(name = "title")
            val title: String,
            @Json(name = "body")
            val body: String,
            @Json(name = "icon")
            val icon: String?,
            @Json(name = "url")
            val url: String?
        )
    }
}