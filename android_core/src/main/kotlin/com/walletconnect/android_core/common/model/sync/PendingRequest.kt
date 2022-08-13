package com.walletconnect.android_core.common.model.sync

data class PendingRequest(
    val requestId: Long,
    val topic: String,
    val method: String,
    val chainId: String?,
    val params: String
)