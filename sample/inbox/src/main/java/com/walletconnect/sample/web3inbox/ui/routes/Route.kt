package com.walletconnect.sample.web3inbox.ui.routes

sealed class Route(val path: String) {
    object SelectAccount : Route("select_account")
    object Home : Route("home")
}
