package com.walletconnect.android_core.common.model.vo.sync

import com.walletconnect.android_core.common.model.type.ClientParams
import com.walletconnect.android_core.common.model.vo.json_rpc.JsonRpcResponseVO
import com.walletconnect.foundation.common.model.Topic

internal data class WCResponseVO(
    val topic: Topic,
    val method: String,
    val response: JsonRpcResponseVO,
    val params: ClientParams,
)