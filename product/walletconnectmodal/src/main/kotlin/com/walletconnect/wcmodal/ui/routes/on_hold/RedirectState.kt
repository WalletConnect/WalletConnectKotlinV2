package com.walletconnect.wcmodal.ui.routes.on_hold

internal sealed class RedirectState {
    object Loading : RedirectState()
    object Reject : RedirectState()
}