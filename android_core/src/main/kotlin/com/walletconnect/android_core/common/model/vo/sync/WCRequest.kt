package com.walletconnect.android_core.common.model.vo.sync

import com.walletconnect.android_core.common.model.type.ClientParams
import com.walletconnect.foundation.common.model.Topic

data class WCRequest(
    val topic: Topic,
    val id: Long,
    val method: String,
    val params: ClientParams
)