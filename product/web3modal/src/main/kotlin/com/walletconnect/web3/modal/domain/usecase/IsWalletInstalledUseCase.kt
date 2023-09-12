package com.walletconnect.web3.modal.domain.usecase

import android.content.Context
import com.walletconnect.android.utils.isWalletInstalled
import com.walletconnect.web3.modal.domain.model.Wallet

class IsWalletInstalledUseCase(
    private val context: Context
) {
    operator fun invoke(wallet: Wallet) = context.packageManager.isWalletInstalled(wallet.appPackage, wallet.mobileLink)
}