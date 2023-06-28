package com.walletconnect.web3.modal.ui

import com.walletconnect.android.internal.common.explorer.data.model.Wallet
import com.walletconnect.web3.modal.ui.navigation.Route

internal sealed class Web3ModalState {
    data class ConnectState(
        val uri: String,
        val wallets: List<Wallet> = listOf(),
    ) : Web3ModalState()

    object SessionState : Web3ModalState()
}

internal fun Web3ModalState.toStartingPath() = when (this) {
    is Web3ModalState.ConnectState -> Route.ConnectYourWallet
    Web3ModalState.SessionState -> Route.Session
}.path