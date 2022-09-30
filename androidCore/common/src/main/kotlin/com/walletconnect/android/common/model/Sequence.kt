package com.walletconnect.android.common.model

import com.walletconnect.foundation.common.model.Topic

interface Sequence {
    val topic: Topic
    val expiry: Expiry
}