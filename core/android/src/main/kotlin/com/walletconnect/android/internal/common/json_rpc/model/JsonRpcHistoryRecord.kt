package com.walletconnect.android.internal.common.json_rpc.model

data class JsonRpcHistoryRecord(
    val id: Long,
    val topic: String,
    val method: String,
    val body: String,
    val response: String?,
)
