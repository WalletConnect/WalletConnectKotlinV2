package com.walletconnect.walletconnectv2.relay.model

import com.walletconnect.walletconnectv2.relay.model.clientsync.ClientParams
import com.walletconnect.walletconnectv2.common.model.vo.TopicVO

internal data class WCRequestSubscriptionPayload(
    val requestId: Long,
    val topic: TopicVO,
    val method: String,
    val params: ClientParams
)