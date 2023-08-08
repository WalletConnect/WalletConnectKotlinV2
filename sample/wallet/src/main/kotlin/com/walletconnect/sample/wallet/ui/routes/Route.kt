package com.walletconnect.sample.wallet.ui.routes

import androidx.navigation.NavController

sealed class Route(val path: String) {
    object GetStarted : Route("get_started")
    object Connections : Route("connections")
    object SessionProposal : Route("session_proposal")
    object SessionRequest : Route("session_request")
    object AuthRequest : Route("auth_request")
    object PasteUri : Route("paste_uri")
    object ScanUri : Route("scan_uri")

    object ConnectionDetails : Route("connection_details")
    object SnackbarMessage : Route("snackbar_message")
    object Web3Inbox : Route("web3inbox")
    object Notifications : Route("notifications_screen")
}

fun NavController.showSnackbar(message: String) {
    navigate("${Route.SnackbarMessage.path}/$message")
}





