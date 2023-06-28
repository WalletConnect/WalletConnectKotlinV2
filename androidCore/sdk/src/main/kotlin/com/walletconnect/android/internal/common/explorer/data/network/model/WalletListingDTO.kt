package com.walletconnect.android.internal.common.explorer.data.network.model

import com.squareup.moshi.Json

data class WalletListingDTO(
    @Json(name = "listings")
    val listings: Map<String, WalletDTO>,
    @Json(name = "count")
    val count: Int,
    @Json(name = "total")
    val total: Int
)