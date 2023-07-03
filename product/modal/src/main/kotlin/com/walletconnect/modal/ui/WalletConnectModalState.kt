package com.walletconnect.modal.ui

import com.walletconnect.android.internal.common.explorer.data.model.Wallet

internal data class WalletConnectModalState(
    val uri: String,
    val wallets: List<Wallet> = listOf()
)