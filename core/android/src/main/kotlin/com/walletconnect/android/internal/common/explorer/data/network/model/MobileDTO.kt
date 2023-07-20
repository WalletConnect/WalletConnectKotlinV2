package com.walletconnect.android.internal.common.explorer.data.network.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MobileDTO(
    @Json(name = "native")
    val native: String?,
    @Json(name = "universal")
    val universal: String?
)