package com.walletconnect.android.internal.common.explorer.data.model

import android.net.Uri

data class Wallet(
    val id: String,
    val name: String,
    val imageUrl: String,
    val nativeLink: String?,
    val universalLink: String?,
    val playStoreLink: String?,
) {
    val appPackage: String? = playStoreLink?.extractPackage()
    var isRecent: Boolean = false
    var isWalletInstalled: Boolean = false
}

private fun String.extractPackage(): String? = Uri.parse(this).getQueryParameter("id")
