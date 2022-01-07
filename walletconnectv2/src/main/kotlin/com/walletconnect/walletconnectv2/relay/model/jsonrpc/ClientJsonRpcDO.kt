package com.walletconnect.walletconnectv2.relay.model.jsonrpc

internal data class ClientJsonRpcDO(
    val id: Long,
    val jsonrpc: String,
    val method: String
)
