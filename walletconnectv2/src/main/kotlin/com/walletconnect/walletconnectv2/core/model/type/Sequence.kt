package com.walletconnect.walletconnectv2.core.model.type

import com.walletconnect.walletconnectv2.core.model.vo.ExpiryVO
import com.walletconnect.walletconnectv2.core.model.vo.TopicVO

internal interface Sequence {
    val topic: TopicVO
    val expiry: ExpiryVO
}