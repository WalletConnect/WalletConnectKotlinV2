package com.walletconnect.sign.core.model.type

import com.walletconnect.android_core.common.model.Expiry
import com.walletconnect.sign.core.model.vo.TopicVO

internal interface Sequence {
    val topic: TopicVO
    val expiry: Expiry
}