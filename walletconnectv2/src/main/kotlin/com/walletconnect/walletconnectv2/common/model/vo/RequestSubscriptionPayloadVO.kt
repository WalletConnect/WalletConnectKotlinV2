package com.walletconnect.walletconnectv2.common.model.vo

import com.walletconnect.walletconnectv2.common.model.type.ClientParams

internal data class RequestSubscriptionPayloadVO(
    val requestId: Long,
    val topic: TopicVO,
    val method: String,
    val params: ClientParams
)