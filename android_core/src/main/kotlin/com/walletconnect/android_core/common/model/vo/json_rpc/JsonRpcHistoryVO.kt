package com.walletconnect.android_core.common.model.vo.json_rpc

data class JsonRpcHistoryVO(
    val requestId: Long,
    val topic: String,
    val method: String,
    val body: String,
    val response: String?,
)