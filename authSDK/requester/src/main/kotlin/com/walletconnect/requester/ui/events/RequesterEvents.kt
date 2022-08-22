package com.walletconnect.requester.ui.events

import com.walletconnect.auth.client.Auth

sealed class RequesterEvents {
    object NoAction : RequesterEvents()
    data class OnReject(val code: Int, val message: String) : RequesterEvents()
    data class OnApprove(val cacao: Auth.Model.Cacao) : RequesterEvents()
}