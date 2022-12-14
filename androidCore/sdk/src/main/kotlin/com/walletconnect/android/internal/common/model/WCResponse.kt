package com.walletconnect.android.internal.common.model

import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.model.type.ClientParams
import com.walletconnect.foundation.common.model.Topic

data class WCResponse(
    val topic: Topic,
    val method: String,
    val response: JsonRpcResponse,
    val params: ClientParams,
)