package com.walletconnect.wcmodal.ui

sealed class WalletConnectModalEvents {
    object SessionApproved: WalletConnectModalEvents()
    object SessionRejected: WalletConnectModalEvents()
    object NoAction: WalletConnectModalEvents()
    object InvalidState: WalletConnectModalEvents()
}