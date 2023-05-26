package com.walletconnect.android.internal.common.explorer.data.network.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MetadataDTO(
    @Json(name = "shortName")
    val shortName: String?,
    @Json(name = "colors")
    val colors: ColorsDTO
)