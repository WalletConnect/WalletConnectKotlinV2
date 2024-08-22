package com.walletconnect.sample.modal.common

const val messageArg = "messageArg"

sealed class Route(val path: String) {
    object Home : Route("Home")

    object Lab : Route("Lab")

    object AlertDialog : Route("Alert")
}