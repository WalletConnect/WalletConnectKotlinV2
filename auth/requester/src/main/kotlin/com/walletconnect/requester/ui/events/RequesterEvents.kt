package com.walletconnect.requester.ui.events

import com.walletconnect.auth.client.Auth

sealed class RequesterEvents {
    object NoAction : RequesterEvents()
    data class OnError(val code: Int, val message: String) : RequesterEvents()
    data class OnAuthenticated(val cacao: Auth.Model.Cacao) : RequesterEvents()
}