package com.walletconnect.web3.modal.ui

sealed class Web3ModalEvents {
    object SessionApproved: Web3ModalEvents()
    object SessionRejected: Web3ModalEvents()
    object NoAction: Web3ModalEvents()
    object InvalidState: Web3ModalEvents()
}