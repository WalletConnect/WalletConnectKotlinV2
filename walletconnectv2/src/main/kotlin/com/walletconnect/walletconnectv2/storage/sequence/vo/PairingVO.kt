package com.walletconnect.walletconnectv2.storage.sequence.vo

import com.walletconnect.walletconnectv2.common.model.Expiry
import com.walletconnect.walletconnectv2.common.model.Topic
import com.walletconnect.walletconnectv2.storage.sequence.SequenceStatus

data class PairingVO(
    val topic: Topic,
    val expiry: Expiry,
    val uri: String,
    val status: SequenceStatus
)