package com.walletconnect.showcase.ui.routes

import androidx.compose.material.ModalBottomSheetDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

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
}

fun NavController.showSnackbar(message: String) {
    navigate("${Route.SnackbarMessage.path}/$message")
}





