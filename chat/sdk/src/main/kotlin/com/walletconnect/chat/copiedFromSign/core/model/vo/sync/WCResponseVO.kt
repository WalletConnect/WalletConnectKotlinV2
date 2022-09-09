package com.walletconnect.chat.copiedFromSign.core.model.vo.sync

import com.walletconnect.chat.copiedFromSign.core.model.type.ClientParams
import com.walletconnect.chat.copiedFromSign.core.model.vo.TopicVO
import com.walletconnect.chat.copiedFromSign.core.model.vo.jsonRpc.JsonRpcResponseVO

internal data class WCResponseVO(
    val topic: TopicVO,
    val method: String,
    val response: JsonRpcResponseVO,
    val params: ClientParams,
)