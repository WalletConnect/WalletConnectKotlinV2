package com.walletconnect.web3.modal.ui

import com.walletconnect.web3.modal.ui.navigation.Route

sealed class Web3ModalState {
    object Loading : Web3ModalState()
    data class ConnectState(val uri: String) : Web3ModalState()
    object SessionState : Web3ModalState()
}

fun Web3ModalState.toStartingPath() = when (this) {
    is Web3ModalState.ConnectState -> Route.ConnectYourWallet
    Web3ModalState.Loading -> Route.Loading
    Web3ModalState.SessionState -> Route.Session
}.path