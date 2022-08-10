package com.walletconnect.sign.core.model.vo.sync

import com.walletconect.android_core.common.model.type.ClientParams
import com.walletconnect.sign.core.model.vo.TopicVO
import com.walletconnect.sign.core.model.vo.jsonRpc.JsonRpcResponseVO

internal data class WCResponseVO(
    val topic: TopicVO,
    val method: String,
    val response: JsonRpcResponseVO,
    val params: ClientParams,
)