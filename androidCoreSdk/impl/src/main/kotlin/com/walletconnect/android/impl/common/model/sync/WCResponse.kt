package com.walletconnect.android.impl.common.model.sync

import com.walletconnect.android.api.JsonRpcResponse
import com.walletconnect.android.impl.common.model.type.ClientParams
import com.walletconnect.foundation.common.model.Topic

data class WCResponse(
    val topic: Topic,
    val method: String,
    val response: JsonRpcResponse,
    val params: ClientParams,
)