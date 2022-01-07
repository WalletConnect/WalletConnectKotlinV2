package com.walletconnect.walletconnectv2.relay.model.jsonrpc

import com.walletconnect.walletconnectv2.relay.model.clientsync.types.ClientParams
import com.walletconnect.walletconnectv2.common.model.vo.TopicVO

internal data class WCRequestSubscriptionPayloadDO(
    val requestId: Long,
    val topic: TopicVO,
    val method: String,
    val params: ClientParams
)