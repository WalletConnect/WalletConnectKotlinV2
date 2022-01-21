package com.walletconnect.walletconnectv2.common.model.type

import com.walletconnect.walletconnectv2.common.model.vo.ExpiryVO
import com.walletconnect.walletconnectv2.common.model.vo.TopicVO
import com.walletconnect.walletconnectv2.storage.sequence.SequenceStatus

internal interface Sequence {
    val topic: TopicVO
    val expiry: ExpiryVO
    val status: SequenceStatus
}