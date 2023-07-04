package com.walletconnect.modal.ui

sealed class WalletConnectModalEvents {
    object SessionApproved: WalletConnectModalEvents()
    object SessionRejected: WalletConnectModalEvents()
    object NoAction: WalletConnectModalEvents()
    object InvalidState: WalletConnectModalEvents()
}