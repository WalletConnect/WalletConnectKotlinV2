package com.walletconnect.walletconnectv2.core.model.vo.jsonRpc

data class JsonRpcHistoryVO(
    val requestId: Long,
    val topic: String,
    val method: String,
    val body: String,
    val response: String?,
)