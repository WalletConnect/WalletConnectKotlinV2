package com.walletconnect.sample.modal.common

sealed class Route(val path: String) {
    object Home : Route("Home")

    object Lab : Route("Lab")
}