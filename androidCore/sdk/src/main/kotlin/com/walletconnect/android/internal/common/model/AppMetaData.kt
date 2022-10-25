package com.walletconnect.android.internal.common.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AppMetaData(
    @Json(name = "name")
    val name: String,
    @Json(name = "description")
    val description: String,
    @Json(name = "url")
    val url: String,
    @Json(name = "icons")
    val icons: List<String>,
    @Json(name = "redirect")
    val redirect: Redirect? = null,
    //todo: Add Type here
)
