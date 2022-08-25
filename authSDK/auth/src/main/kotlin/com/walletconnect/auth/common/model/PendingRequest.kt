package com.walletconnect.auth.common.model

import com.walletconnect.auth.common.json_rpc.payload.PayloadParamsDTO
import com.walletconnect.foundation.common.model.Topic

internal data class PendingRequest(
    val id: Long,
    val topic: Topic,
    val method: String,
    val payloadParams: PayloadParamsDTO
)