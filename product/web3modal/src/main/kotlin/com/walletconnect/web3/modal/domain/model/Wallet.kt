package com.walletconnect.web3.modal.domain.model

import android.net.Uri

data class Wallet(
    val id: String,
    val name: String,
    val homePage: String,
    val imageUrl: String,
    val order: String,
    val mobileLink: String?,
    val playStore: String?,
    val isRecommended: Boolean = false
) {
    val appPackage: String? = playStore?.extractPackage()
    var isRecent: Boolean = false
    var isWalletInstalled: Boolean = false
}

private fun String.extractPackage(): String? = Uri.parse(this).getQueryParameter("id")
