package com.walletconnect.auth.common.model

import com.walletconnect.auth.common.json_rpc.payload.PayloadParamsDTO

internal data class PendingRequest(
    val requestId: Long,
    val payloadParams: PayloadParamsDTO,
    val response: String?
)