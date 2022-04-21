package com.walletconnect.wallet.ui.host.request

sealed class SessionRequestUI {

    object Initial : SessionRequestUI()

    data class Content(
        val topic: String,
        val icon: String?,
        val peerName: String?,
        val requestId: Long,
        val param: String,
        val chain: String?,
        val method: String,
    ) : SessionRequestUI()
}
