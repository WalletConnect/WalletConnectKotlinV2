package com.walletconnect.sign.common.model

import com.walletconnect.foundation.common.model.Topic

data class PendingRequest(
    val id: Long,
    val topic: Topic,
    val method: String,
    val chainId: String?,
    val params: String
)