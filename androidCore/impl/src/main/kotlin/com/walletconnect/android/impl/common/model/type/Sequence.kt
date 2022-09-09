package com.walletconnect.android.impl.common.model.type

import com.walletconnect.android.common.model.Expiry
import com.walletconnect.foundation.common.model.Topic

interface Sequence {
    val topic: Topic
    val expiry: Expiry
}