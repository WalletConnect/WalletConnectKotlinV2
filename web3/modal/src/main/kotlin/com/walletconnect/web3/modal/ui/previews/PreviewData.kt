package com.walletconnect.web3.modal.ui.previews

import com.walletconnect.sample.dapp.web3modal.network.model.WalletIcons
import com.walletconnect.web3.modal.domain.model.Wallet

internal val walletsRecommendations = listOf<Wallet>(
    Wallet(
        "1",
        "Rainbow",
        "https://explorer-api.walletconnect.com/v3/logo/md/7a33d7f1-3d12-4b5c-f3ee-5cd83cb1b500?projectId=a7f155fbc59c18b6ad4fb5650067dd41".toWalletIcons(),
        "rainbow://",
        "https://rnbwapp.com",
        "https://play.google.com/store/apps/details?id=me.rainbow"
    ),
    Wallet(
        "2",
        "Safe",
        "https://explorer-api.walletconnect.com/v3/logo/md/a1cb2777-f8f9-49b0-53fd-443d20ee0b00?projectId=a7f155fbc59c18b6ad4fb5650067dd41".toWalletIcons(),
        "",
        "https://app.safe.global/",
        "https://play.google.com/store/apps/details?id=io.gnosis.safe"
    ),
    Wallet(
        "3",
        "Uniswap Wallet",
        "https://explorer-api.walletconnect.com/v3/logo/md/bff9cf1f-df19-42ce-f62a-87f04df13c00?projectId=a7f155fbc59c18b6ad4fb5650067dd41".toWalletIcons(),
        "",
        "https://uniswap.org/app",
        null
    ),
    Wallet(
        "4",
        "CoolWallet",
        "https://explorer-api.walletconnect.com/v3/logo/md/7a33d7f1-3d12-4b5c-f3ee-5cd83cb1b500?projectId=a7f155fbc59c18b6ad4fb5650067dd41".toWalletIcons(),
        "coolwallet://",
        null,
        "https://play.google.com/store/apps/details?id=com.coolbitx.cwsapp"
    )
)

private fun String.toWalletIcons() = WalletIcons(this, this, this)