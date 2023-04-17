package com.walletconnect.sample.dapp.web3modal.ui

sealed class Route(val path: String) {

    object Web3Modal : Route("web3_modal")
    object ConnectYourWallet : Route("connect_your_wallet")
    object ScanQRCode : Route("scan_the_code")
    object Help : Route("modal_help")
    object Search : Route("search")
}