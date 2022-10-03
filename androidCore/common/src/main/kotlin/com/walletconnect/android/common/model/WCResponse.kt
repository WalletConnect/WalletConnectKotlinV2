package com.walletconnect.android.common.model

import com.walletconnect.android.common.JsonRpcResponse
import com.walletconnect.foundation.common.model.Topic

data class WCResponse(
    val topic: Topic,
    val method: String,
    val response: JsonRpcResponse,
    val params: ClientParams,
)