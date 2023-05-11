package com.walletconnect.web3.modal.domain.model

import com.walletconnect.web3.modal.network.model.ExplorerWalletResponse

internal data class Wallet(
    val id: String,
    val name: String,
    val imageId: String,
    val nativeLink: String,
    val universalLink: String?,
    // TODO Playstore link should be non null when `platforms` parameter will be fixed in the api
    val playStoreLink: String?,
) {
    val imageUrl: String
        get() = "https://explorer-api.walletconnect.com/w3m/v1/getWalletImage/$imageId"
}

internal fun ExplorerWalletResponse.toWallet() = Wallet(
    id = id,
    name = name,
    imageId = imageId,
    nativeLink = mobile.native,
    universalLink = mobile.universal,
    playStoreLink = app.android
)
