package com.walletconnect.responder.ui.events

import com.walletconnect.auth.client.Auth

sealed class ResponderEvents {
    object NoAction : ResponderEvents()
    data class OnRequest(val id: Long, val response: Auth.Model.Response) : ResponderEvents()
}