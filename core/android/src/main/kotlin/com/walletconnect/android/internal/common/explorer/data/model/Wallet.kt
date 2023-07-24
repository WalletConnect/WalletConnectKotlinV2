package com.walletconnect.android.internal.common.explorer.data.model

data class Wallet(
    val id: String,
    val name: String,
    val imageUrl: String,
    val nativeLink: String?,
    val universalLink: String?,
    val playStoreLink: String?,
) {
    var recent: Boolean = false
}