package com.walletconnect.android.internal.common.json_rpc.model

import com.walletconnect.android.internal.common.model.TransportType

data class JsonRpcHistoryRecord(
    val id: Long,
    val topic: String,
    val method: String,
    val body: String,
    val response: String?,
    val transportType: TransportType?
)
