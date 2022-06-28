package com.walletconnect.chat.copiedFromSign.core.model.vo.sync

import com.walletconnect.chat.copiedFromSign.core.model.type.ClientParams
import com.walletconnect.chat.copiedFromSign.core.model.vo.TopicVO

internal data class WCRequestVO(
    val topic: TopicVO,
    val id: Long,
    val method: String,
    val params: ClientParams
)