package com.walletconnect.sample.dapp.web3modal.ui.routes.connect_wallet

import com.walletconnect.sample.dapp.web3modal.domain.model.WalletRecommendation

sealed class ConnectYourWalletUI {
    object Loading : ConnectYourWalletUI()
    data class SelectWallet(val wallets: List<WalletRecommendation>) : ConnectYourWalletUI()
    object Empty : ConnectYourWalletUI()
}