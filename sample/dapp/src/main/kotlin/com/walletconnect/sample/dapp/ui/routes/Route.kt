package com.walletconnect.sample.dapp.ui.routes

sealed class Route(val path: String) {
    object ChainSelection : Route("chain_selection")
    object ParingSelection : Route("paring_selection")
    object ParingGeneration : Route("paring_generation")
    object Session : Route("session")
    object Account : Route("account")

    object MessageDialog : Route("message_dialog")
}
