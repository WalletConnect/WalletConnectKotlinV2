package com.walletconnect.sign.core.model.vo.sync

import com.walletconnect.android_core.common.model.type.ClientParams
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.sign.core.model.vo.jsonRpc.JsonRpcResponseVO

internal data class WCResponseVO(
    val topic: Topic,
    val method: String,
    val response: JsonRpcResponseVO,
    val params: ClientParams,
)