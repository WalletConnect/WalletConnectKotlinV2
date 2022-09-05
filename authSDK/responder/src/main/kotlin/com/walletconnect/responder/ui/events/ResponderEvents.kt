package com.walletconnect.responder.ui.events


sealed class ResponderEvents {
    object NoAction : ResponderEvents()
    data class OnRequest(val id: Long, val message: String) : ResponderEvents()
}