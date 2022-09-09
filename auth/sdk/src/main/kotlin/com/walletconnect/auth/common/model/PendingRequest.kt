package com.walletconnect.auth.common.model

internal data class PendingRequest(
    val id: Long,
    val payloadParams: PayloadParams,
    val message: String,
)
