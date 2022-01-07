package com.walletconnect.walletconnectv2.relay.model

internal data class ClientJsonRpc(
    val id: Long,
    val jsonrpc: String,
    val method: String
)
