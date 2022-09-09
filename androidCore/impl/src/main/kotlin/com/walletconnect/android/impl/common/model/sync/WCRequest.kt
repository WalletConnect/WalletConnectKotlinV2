package com.walletconnect.android.impl.common.model.sync

import com.walletconnect.android.impl.common.model.type.ClientParams
import com.walletconnect.foundation.common.model.Topic

data class WCRequest(
    val topic: Topic,
    val id: Long,
    val method: String,
    val params: ClientParams
)