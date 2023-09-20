package com.walletconnect.modals.common

sealed class Route(val path: String) {
    object Home : Route("Home")

    object Lab : Route("Lab")
}