package com.walletconnect.walletconnectv2.core.model.vo.sync

import com.walletconnect.walletconnectv2.core.model.type.ClientParams
import com.walletconnect.walletconnectv2.core.model.vo.TopicVO
import com.walletconnect.walletconnectv2.core.model.vo.jsonRpc.JsonRpcResponseVO

internal data class WCResponseVO(
    val topic: TopicVO,
    val method: String,
    val response: JsonRpcResponseVO,
    val params: ClientParams,
)