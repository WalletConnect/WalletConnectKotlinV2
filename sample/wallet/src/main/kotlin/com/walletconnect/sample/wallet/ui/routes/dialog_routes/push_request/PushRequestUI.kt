package com.walletconnect.sample.wallet.ui.routes.dialog_routes.push_request

import com.walletconnect.sample.wallet.ui.common.peer.PeerUI

data class PushRequestUI(
    val requestId: Long,
    val peerUI: PeerUI,
)