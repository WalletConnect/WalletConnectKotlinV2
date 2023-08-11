package com.walletconnect.web3.modal.ui

import com.walletconnect.android.internal.common.explorer.data.model.Wallet
import com.walletconnect.web3.modal.ui.navigation.Route

internal sealed class Web3ModalState {
    data class Connect(
        val uri: String,
        val wallets: List<Wallet> = listOf(),
    ) : Web3ModalState()

    object Loading: Web3ModalState()
    data class Error(val error: Throwable): Web3ModalState()
    object SessionState : Web3ModalState()
}

internal fun Web3ModalState.toStartingPath() = when (this) {
    is Web3ModalState.Connect -> Route.CONNECT_YOUR_WALLET
    Web3ModalState.SessionState -> Route.SESSION
    else -> Route.WEB3MODAL
}.path