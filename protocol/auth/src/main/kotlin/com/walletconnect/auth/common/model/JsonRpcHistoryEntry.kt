package com.walletconnect.auth.common.model

import com.walletconnect.auth.common.json_rpc.AuthParams
import com.walletconnect.foundation.common.model.Topic

internal data class JsonRpcHistoryEntry(
    val id: Long,
    val topic: Topic,
    val method: String,
    val params: AuthParams.RequestParams,
    val response: String?
)