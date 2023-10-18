package com.walletconnect.sample.modal.common

import androidx.navigation.NavController

const val messageArg = "messageArg"

sealed class Route(val path: String) {
    object Home : Route("Home")

    object Lab : Route("Lab")

    object AlertDialog : Route("Alert")
}

fun NavController.openAlertDialog(message: String) {
    navigate(Route.AlertDialog.path + "/$message")
}