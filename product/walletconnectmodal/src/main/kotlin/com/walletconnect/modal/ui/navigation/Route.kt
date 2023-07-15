package com.walletconnect.modal.ui.navigation

internal sealed class Route(val path: String) {

    object WalletConnectModalRoot: Route("wc_modal_root")

    object ConnectYourWallet: Route("connect_wallet")

    object ScanQRCode : Route("scan_qr_code")

    object Help : Route("help")

    object GetAWallet : Route("get_a_wallet")

    object AllWallets : Route("all_wallets")

}