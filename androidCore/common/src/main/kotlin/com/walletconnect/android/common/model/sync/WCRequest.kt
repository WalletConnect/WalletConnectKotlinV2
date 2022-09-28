package com.walletconnect.android.common.model.sync

import com.walletconnect.android.common.model.type.ClientParams
import com.walletconnect.foundation.common.model.Topic

data class WCRequest(
    val topic: Topic,
    val id: Long,
    val method: String,
    val params: ClientParams
)