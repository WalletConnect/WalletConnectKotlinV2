package com.walletconnect.sign.core.model.type

import com.walletconect.android_core.common.model.Expiry
import com.walletconnect.sign.core.model.vo.TopicVO

internal interface Sequence {
    val topic: TopicVO
    val expiry: Expiry
}