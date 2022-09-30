package com.walletconnect.android.common.model

import com.walletconnect.foundation.common.model.Topic

data class WCRequest(
    val topic: Topic,
    val id: Long,
    val method: String,
    val params: ClientParams
)