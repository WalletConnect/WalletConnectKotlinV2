package com.walletconnect.sample.wallet.ui.routes.dialog_routes.session_request

import com.walletconnect.sample.wallet.ui.common.peer.PeerContextUI
import com.walletconnect.sample.wallet.ui.common.peer.PeerUI

sealed class SessionRequestUI {
    object Initial : SessionRequestUI()

    data class Content(
        val peerUI: PeerUI,
        val topic: String,
        val requestId: Long,
        val param: String,
        val chain: String?,
        val method: String,
        val peerContextUI: PeerContextUI
    ) : SessionRequestUI()
}