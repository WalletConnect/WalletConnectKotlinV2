package com.walletconnect.web3.modal.domain.model

import com.walletconnect.sample.dapp.web3modal.network.model.ExplorerWalletResponse
import com.walletconnect.sample.dapp.web3modal.network.model.WalletIcons

internal data class Wallet(
    val id: String,
    val name: String,
    val images: WalletIcons,
    val nativeLink: String,
    val universalLink: String?,
    val playStoreLink: String?,
)

internal fun ExplorerWalletResponse.toWallet() = Wallet(
    id = id,
    name = name,
    images = images,
    nativeLink = mobile.native,
    universalLink = mobile.universal,
    playStoreLink = app.android
)
