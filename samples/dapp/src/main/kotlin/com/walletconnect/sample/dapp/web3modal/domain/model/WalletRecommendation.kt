package com.walletconnect.sample.dapp.web3modal.domain.model

import com.walletconnect.sample.dapp.web3modal.network.model.ExplorerWalletResponse
import com.walletconnect.sample.dapp.web3modal.network.model.WalletIcons

data class WalletRecommendation(
    val id: String,
    val name: String,
    val images: WalletIcons,
    val nativeLink: String,
    val universalLink: String?,
    val playStoreLink: String?,
)

fun ExplorerWalletResponse.toWalletRecommendation() = WalletRecommendation(
    id = id,
    name = name,
    images = images,
    nativeLink = mobile.native,
    universalLink = mobile.universal,
    playStoreLink = app.android
)