package com.walletconnect.notify.common.model

import com.walletconnect.foundation.common.model.Topic

internal sealed interface TimeoutInfo {
    data class Data(val requestId: Long, val topic: Topic) : TimeoutInfo
    object Nothing : TimeoutInfo
}