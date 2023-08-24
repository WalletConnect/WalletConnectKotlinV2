package com.walletconnect.web3.modal.ui.routes.connect.redirect

sealed class RedirectState {
    object Loading: RedirectState()
    object Reject: RedirectState()

}