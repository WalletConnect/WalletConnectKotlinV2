package com.walletconnect.android_core.common.model.sync

import com.walletconnect.android_core.common.model.type.ClientParams
import com.walletconnect.android_core.common.model.json_rpc.JsonRpcResponse
import com.walletconnect.foundation.common.model.Topic

data class WCResponse(
    val topic: Topic,
    val method: String,
    val response: JsonRpcResponse,
    val params: ClientParams,
)