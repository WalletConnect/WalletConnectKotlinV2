package com.walletconnect.web3.wallet.ui.routes.dialog_routes.push_request

import com.walletconnect.web3.wallet.ui.common.peer.PeerUI

data class PushRequestUI(
    val requestId: Long,
    val peerUI: PeerUI,
)