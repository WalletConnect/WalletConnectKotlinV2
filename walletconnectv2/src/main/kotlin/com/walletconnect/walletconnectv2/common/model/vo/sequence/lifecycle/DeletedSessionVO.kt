package com.walletconnect.walletconnectv2.common.model.vo.sequence.lifecycle

import com.walletconnect.walletconnectv2.common.model.type.SequenceLifecycle

data class DeletedSessionVO(
    val topic: String,
    val reason: String
) : SequenceLifecycle
