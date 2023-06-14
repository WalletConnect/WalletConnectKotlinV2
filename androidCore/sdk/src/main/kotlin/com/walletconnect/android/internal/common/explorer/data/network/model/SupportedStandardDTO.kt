package com.walletconnect.android.internal.common.explorer.data.network.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SupportedStandardDTO(
    @Json(name = "id")
    val id: String,
    @Json(name = "url")
    val url: String,
    @Json(name = "title")
    val title: String,
    @Json(name = "standard_id")
    val standardId: Int,
    @Json(name = "standard_prefix")
    val standardPrefix: String
)