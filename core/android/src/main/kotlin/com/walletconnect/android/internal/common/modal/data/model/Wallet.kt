package com.walletconnect.android.internal.common.modal.data.model

import android.net.Uri

data class Wallet(
    val id: String,
    val name: String,
    val homePage: String,
    val imageUrl: String,
    val order: String,
    val mobileLink: String?,
    val playStore: String?,
    val webAppLink: String?,
    val isRecommended: Boolean = false
) {
    val appPackage: String? = playStore?.extractPackage()
    var isRecent: Boolean = false
    var isWalletInstalled: Boolean = false

    val hasMobileWallet: Boolean
        get() = mobileLink != null

    val hasWebApp: Boolean
        get() = webAppLink != null
}

private fun String.extractPackage(): String? = Uri.parse(this).getQueryParameter("id")
