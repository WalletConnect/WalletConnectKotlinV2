package com.walletconnect.sign.common.model

import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.foundation.common.model.Topic

internal data class PendingRequest<T>(
    val id: Long,
    val topic: Topic,
    val method: String,
    val chainId: String?,
    val params: T,
    val expiry: Expiry? = null
)