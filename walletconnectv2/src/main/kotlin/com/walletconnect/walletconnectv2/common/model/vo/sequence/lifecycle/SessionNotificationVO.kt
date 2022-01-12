package com.walletconnect.walletconnectv2.common.model.vo.sequence.lifecycle

import com.walletconnect.walletconnectv2.common.model.type.SequenceLifecycle

data class SessionNotificationVO(
    val topic: String,
    val type: String,
    val data: String
) : SequenceLifecycle
