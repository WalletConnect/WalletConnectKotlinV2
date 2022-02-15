package com.walletconnect.walletconnectv2.core.model.vo

import com.walletconnect.walletconnectv2.core.model.type.ClientParams

internal data class RequestSubscriptionPayloadVO(
    val params: ClientParams,
    val request: WCRequestVO
)

internal data class WCRequestVO(
    val topic: TopicVO,
    val id: Long,
    val method: String
)