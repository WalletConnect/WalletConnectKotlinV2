package com.walletconnect.sign.core.model.vo.sync

data class PendingRequestVO(
    val requestId: Long,
    val topic: String,
    val method: String,
    val chainId: String?,
    val params: String
)