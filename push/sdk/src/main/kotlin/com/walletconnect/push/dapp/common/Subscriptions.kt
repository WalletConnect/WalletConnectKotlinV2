package com.walletconnect.push.dapp.common

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.android.internal.common.model.AppMetaData

@JsonClass(generateAdapter = true)
data class Subscriptions(
    @Json(name = "topic")
    val topic: String,
    @Json(name = "relay")
    val relay: Relay,
    @Json(name = "metadata")
    val metadata: AppMetaData
) {
    @JsonClass(generateAdapter = true)
    data class Relay(
        @Json(name = "protocol")
        val protocol: String,
        @Json(name = "data")
        val `data`: String
    )
}