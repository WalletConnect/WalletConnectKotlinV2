package com.walletconnect.auth.common.model

internal data class PendingRequest(
    val id: Long,
    val pairingTopic: String,
    val payloadParams: PayloadParams
)
