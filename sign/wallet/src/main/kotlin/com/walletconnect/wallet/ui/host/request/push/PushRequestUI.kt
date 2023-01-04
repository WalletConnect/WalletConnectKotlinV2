package com.walletconnect.wallet.ui.host.request.push

sealed class PushRequestUI {

    object Initial : PushRequestUI()

    data class Content(
        val requestId: Long,
        val peerName: String?,
        val peerDesc: String?,
        val icon: String?
    ) : PushRequestUI()
}
