package com.walletconnect.sign.core.model.type

import com.walletconnect.sign.core.model.vo.ExpiryVO
import com.walletconnect.sign.core.model.vo.TopicVO

internal interface Sequence {
    val topic: TopicVO
    val expiry: ExpiryVO
}