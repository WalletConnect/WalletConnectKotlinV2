package com.walletconnect.walletconnectv2.common.model.vo.sequence

import com.walletconnect.walletconnectv2.common.model.type.Sequence
import com.walletconnect.walletconnectv2.common.model.vo.ExpiryVO
import com.walletconnect.walletconnectv2.common.model.vo.TopicVO
import com.walletconnect.walletconnectv2.storage.sequence.SequenceStatus

internal data class PairingVO(
    override val topic: TopicVO,
    override val expiry: ExpiryVO,
    val uri: String,
    override val status: SequenceStatus
): Sequence