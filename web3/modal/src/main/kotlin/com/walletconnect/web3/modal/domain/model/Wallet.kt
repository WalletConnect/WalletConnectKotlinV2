package com.walletconnect.web3.modal.domain.model

import com.walletconnect.web3.modal.network.model.ExplorerWalletResponse

private const val GET_WALLET_IMAGE_URL = "https://explorer-api.walletconnect.com/w3m/v1/getWalletImage"

internal data class Wallet(
    val id: String,
    val name: String,
    val imageId: String,
    val nativeLink: String,
    val universalLink: String?,
    val playStoreLink: String,
) {
    val imageUrl: String
        get() = "GET_WALLET_IMAGE_URL/$imageId"
}

internal fun ExplorerWalletResponse.toWallet() = Wallet(
    id = id,
    name = name,
    imageId = imageId,
    nativeLink = mobile.native,
    universalLink = mobile.universal,
    playStoreLink = app.android
)
