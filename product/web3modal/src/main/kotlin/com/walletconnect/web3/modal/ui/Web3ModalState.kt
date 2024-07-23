package com.walletconnect.web3.modal.ui

internal sealed class Web3ModalState {
    object Connect : Web3ModalState()

    object Loading : Web3ModalState()

    data class Error(val error: Throwable) : Web3ModalState()

    object AccountState : Web3ModalState()
}