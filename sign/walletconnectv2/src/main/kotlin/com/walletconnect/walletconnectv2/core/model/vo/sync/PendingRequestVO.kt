package com.walletconnect.walletconnectv2.core.model.vo.sync

data class PendingRequestVO(
    val requestId: Long,
    val topic: String,
    val method: String,
    val chainId: String?,
    val params: String
)