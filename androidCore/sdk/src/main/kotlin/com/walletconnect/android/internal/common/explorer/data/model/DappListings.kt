package com.walletconnect.android.internal.common.explorer.data.model

data class DappListings(
    val listings: Map<String, Listing>,
    val count: Int,
    val total: Int
)