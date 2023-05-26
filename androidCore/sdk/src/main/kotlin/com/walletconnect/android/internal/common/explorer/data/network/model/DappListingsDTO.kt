package com.walletconnect.android.internal.common.explorer.data.network.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DappListingsDTO(
    @Json(name = "listings")
    val listings: Map<String, ListingDTO>,
    @Json(name = "count")
    val count: Int,
    @Json(name = "total")
    val total: Int
)