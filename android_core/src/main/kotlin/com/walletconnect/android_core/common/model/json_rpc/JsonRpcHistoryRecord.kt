package com.walletconnect.android_core.common.model.json_rpc

 data class JsonRpcHistoryRecord(
    val id: Long,
    val topic: String,
    val method: String,
    val body: String,
    val response: String?,
)