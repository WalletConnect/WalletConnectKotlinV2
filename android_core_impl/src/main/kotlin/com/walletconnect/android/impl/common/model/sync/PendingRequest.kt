package com.walletconnect.android.impl.common.model.sync

data class PendingRequest(
    val requestId: Long,
    val topic: String,
    val method: String,
    val chainId: String?,
    val params: String
)