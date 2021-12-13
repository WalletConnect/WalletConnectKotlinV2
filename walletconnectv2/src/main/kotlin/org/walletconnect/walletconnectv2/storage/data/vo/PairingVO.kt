package org.walletconnect.walletconnectv2.storage.data.vo

import org.walletconnect.walletconnectv2.common.Expiry
import org.walletconnect.walletconnectv2.common.Topic
import org.walletconnect.walletconnectv2.storage.SequenceStatus

data class PairingVO(
    val topic: Topic,
    val expiry: Expiry,
    val uri: String,
    val status: SequenceStatus
)