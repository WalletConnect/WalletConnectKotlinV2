package com.walletconnect.walletconnectv2.storage.sequence.vo

import com.walletconnect.walletconnectv2.common.model.vo.ExpiryVO
import com.walletconnect.walletconnectv2.common.model.vo.TopicVO
import com.walletconnect.walletconnectv2.storage.sequence.SequenceStatus

data class PairingVO(
    val topic: TopicVO,
    val expiry: ExpiryVO,
    val uri: String,
    val status: SequenceStatus
)