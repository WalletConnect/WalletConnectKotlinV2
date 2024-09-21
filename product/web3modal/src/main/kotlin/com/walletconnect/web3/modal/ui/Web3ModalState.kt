package com.walletconnect.web3.modal.ui
@Deprecated("com.walletconnect.web3.modal.ui.Web3ModalState has been deprecated. Please use com.reown.appkit.modal.ui.AppKitState instead from - https://github.com/reown-com/reown-kotlin")
internal sealed class Web3ModalState {
    object Connect : Web3ModalState()

    object Loading : Web3ModalState()

    data class Error(val error: Throwable) : Web3ModalState()

    object AccountState : Web3ModalState()
}