package com.walletconnect.walletconnectv2.relay.model

data class ClientJsonRpc(
    val id: Long,
    val jsonrpc: String,
    val method: String
)
