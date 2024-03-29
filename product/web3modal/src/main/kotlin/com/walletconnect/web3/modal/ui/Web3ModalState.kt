package com.walletconnect.web3.modal.ui

import com.walletconnect.web3.modal.ui.navigation.Route

internal sealed class Web3ModalState {
    object Connect : Web3ModalState()

    object Loading : Web3ModalState()

    data class Error(val error: Throwable) : Web3ModalState()

    object AccountState : Web3ModalState()
}

internal fun Web3ModalState.toStartingPath() = when (this) {
    is Web3ModalState.Connect -> Route.CONNECT_YOUR_WALLET
    is Web3ModalState.AccountState -> Route.ACCOUNT
    else -> Route.WEB3MODAL
}.path