package com.walletconnect.sample.dapp.web3modal.ui

sealed class Web3ModalEvents {
    object SessionApproved: Web3ModalEvents()
    object SessionRejected: Web3ModalEvents()
    object NoAction: Web3ModalEvents()
}