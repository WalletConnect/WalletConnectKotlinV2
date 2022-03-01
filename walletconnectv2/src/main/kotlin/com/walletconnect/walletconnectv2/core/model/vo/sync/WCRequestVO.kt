package com.walletconnect.walletconnectv2.core.model.vo.sync

import com.walletconnect.walletconnectv2.core.model.type.ClientParams
import com.walletconnect.walletconnectv2.core.model.vo.TopicVO

internal data class WCRequestVO(
    val topic: TopicVO,
    val id: Long,
    val method: String,
    val params: ClientParams
)