package com.walletconnect.android.internal.common.explorer.data.network.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ImageUrlDTO(
    @Json(name = "sm")
    val sm: String,
    @Json(name = "md")
    val md: String,
    @Json(name = "lg")
    val lg: String
)