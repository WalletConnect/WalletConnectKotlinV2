package com.walletconnect.walletconnectv2.core.model.vo.sync

import com.walletconnect.walletconnectv2.storage.history.model.JsonRpcStatus

data class PendingRequestVO(
    val requestId: Long,
    val topic: String,
    val method: String,
    val chainId: String?,
    val params: String,
    val jsonRpcStatus: JsonRpcStatus,
)