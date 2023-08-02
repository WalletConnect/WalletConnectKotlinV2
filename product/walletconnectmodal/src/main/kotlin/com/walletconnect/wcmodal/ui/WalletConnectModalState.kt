package com.walletconnect.wcmodal.ui

import com.walletconnect.android.internal.common.explorer.data.model.Wallet

internal sealed class WalletConnectModalState {
    data class Connect(
        val uri: String,
        val wallets: List<Wallet> = listOf()
    ): WalletConnectModalState()
    object Loading: WalletConnectModalState()
    data class Error(val error: Throwable): WalletConnectModalState()
}