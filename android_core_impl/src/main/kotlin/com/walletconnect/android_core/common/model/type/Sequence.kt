package com.walletconnect.android_core.common.model.type

import com.walletconnect.android_core.common.model.Expiry
import com.walletconnect.foundation.common.model.Topic

interface Sequence {
    val topic: Topic
    val expiry: Expiry
}