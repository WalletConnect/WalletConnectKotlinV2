package com.walletconnect.walletconnectv2.relay.model

import com.walletconnect.walletconnectv2.relay.model.clientsync.ClientParams
import com.walletconnect.walletconnectv2.common.model.Topic

internal data class WCRequestSubscriptionPayload(
    val requestId: Long,
    val topic: Topic,
    val method: String,
    val params: ClientParams
)