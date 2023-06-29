package com.walletconnect.android.internal.common.explorer.data.network.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AppDTO(
    @Json(name = "browser")
    val browser: String?,
    @Json(name = "ios")
    val ios: String?,
    @Json(name = "android")
    val android: String?,
    @Json(name = "mac")
    val mac: String?,
    @Json(name = "windows")
    val windows: String?,
    @Json(name = "linux")
    val linux: String?,
    @Json(name = "chrome")
    val chrome: String?,
    @Json(name = "firefox")
    val firefox: String?,
    @Json(name = "safari")
    val safari: String?,
    @Json(name = "edge")
    val edge: String?,
    @Json(name = "opera")
    val opera: String?
)