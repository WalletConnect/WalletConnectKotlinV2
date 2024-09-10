package com.walletconnect.web3.modal.ui

@Deprecated("com.walletconnect.web3.modal.ui.Web3ModalEvents has been deprecated. Please use com.reown.appkit.modal.ui.Web3ModalEvents instead from - https://github.com/reown-com/reown-kotlin")
sealed class Web3ModalEvents {
    object SessionApproved: Web3ModalEvents()
    object SessionRejected: Web3ModalEvents()
    object NoAction: Web3ModalEvents()
    object InvalidState: Web3ModalEvents()
}