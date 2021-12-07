package org.walletconnect.walletconnectv2.jsonrpc.model

import org.walletconnect.walletconnectv2.ClientParams
import org.walletconnect.walletconnectv2.common.Topic

data class WCRequestSubscriptionPayload(
    val requestId: Long,
    val topic: Topic,
    val method: String,
    val params: ClientParams
)

data class ClientJsonRpc(
    val id: Long,
    val jsonrpc: String,
    val method: String
)