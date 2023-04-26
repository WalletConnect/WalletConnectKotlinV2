package com.walletconnect.sample.dapp.web3modal.ui

internal const val chainsNavArgId = "ChainsNavArgId"

sealed class Route(val path: String) {
    object Web3Modal : Route("web3_modal/$chainsNavArgId")
    object ConnectYourWallet : Route("connect_your_wallet")
    object ScanQRCode : Route("scan_the_code")
    object Help : Route("modal_help")
    object GetAWallet : Route("get_a_wallet")
}
