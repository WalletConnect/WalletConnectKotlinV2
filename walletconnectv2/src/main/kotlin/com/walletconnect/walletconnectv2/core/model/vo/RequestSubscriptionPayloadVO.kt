package com.walletconnect.walletconnectv2.core.model.vo

import com.walletconnect.walletconnectv2.core.model.type.ClientParams

internal data class RequestSubscriptionPayloadVO(
    val requestId: Long,
    val topic: TopicVO,
    val method: String,
    val params: ClientParams
)