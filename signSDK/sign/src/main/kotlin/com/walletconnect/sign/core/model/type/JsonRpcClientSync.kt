package com.walletconnect.sign.core.model.type

internal interface JsonRpcClientSync<T : ClientParams> : SerializableJsonRpc {
    val id: Long
    val method: String
    val jsonrpc: String
    val params: T
}