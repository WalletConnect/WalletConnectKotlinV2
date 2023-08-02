package com.walletconnect.android.internal.common.model

import com.walletconnect.android.internal.common.model.type.ClientParams
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.utils.Empty

data class WCRequest(
    val topic: Topic,
    val id: Long,
    val method: String,
    val params: ClientParams,
    val message: String = String.Empty
)