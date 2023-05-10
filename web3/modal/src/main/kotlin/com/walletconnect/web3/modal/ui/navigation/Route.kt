package com.walletconnect.web3.modal.ui.navigation

//Todo Think about split it into own graphs routes

internal sealed class Route(val path: String) {
    //Common
    object Web3Modal : Route("web3_modal")
    object Loading : Route("loading")

    //Connect routes
    object ConnectYourWallet : Route("connect_your_wallet")
    object ScanQRCode : Route("scan_the_code")
    object Help : Route("modal_help")
    object GetAWallet : Route("get_a_wallet")

    //Session routes
    object Session : Route("session")
}
