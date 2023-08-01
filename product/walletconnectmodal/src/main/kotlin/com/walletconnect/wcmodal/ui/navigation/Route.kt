package com.walletconnect.wcmodal.ui.navigation

internal sealed class Route(val path: String) {

    object WalletConnectModalRoot: Route("wc_modal_root")

    object ConnectYourWallet: Route("connect_wallet")

    object ScanQRCode : Route("scan_qr_code")

    object Help : Route("help")

    object GetAWallet : Route("get_a_wallet")

    object AllWallets : Route("all_wallets")

    object OnHold : Route("on_hold") {
        const val walletIdKey = "walletId"
        const val walletIdArg = "{walletId}"
    }
}
