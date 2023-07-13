package com.walletconnect.android.internal.common.explorer.data.model

data class WalletListing(
    val listing: List<Wallet>,
    val count: Int,
    val total: Int
)