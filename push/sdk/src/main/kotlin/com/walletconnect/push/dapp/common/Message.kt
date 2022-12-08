package com.walletconnect.push.dapp.common


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Message(
    @Json(name = "title")
    val title: String,
    @Json(name = "body")
    val body: String,
    @Json(name = "icon")
    val icon: String,
    @Json(name = "url")
    val url: String
)