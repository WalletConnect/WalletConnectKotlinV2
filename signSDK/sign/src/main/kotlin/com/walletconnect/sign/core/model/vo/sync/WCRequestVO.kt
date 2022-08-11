package com.walletconnect.sign.core.model.vo.sync

import com.walletconnect.android_core.common.model.type.ClientParams
import com.walletconnect.foundation.common.model.Topic

internal data class WCRequestVO(
    val topic: Topic,
    val id: Long,
    val method: String,
    val params: ClientParams
)