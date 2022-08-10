package com.walletconnect.sign.core.model.vo.sync

import com.walletconect.android_core.common.model.type.ClientParams
import com.walletconnect.sign.core.model.vo.TopicVO

internal data class WCRequestVO(
    val topic: TopicVO,
    val id: Long,
    val method: String,
    val params: ClientParams
)