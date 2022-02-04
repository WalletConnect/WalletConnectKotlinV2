package com.walletconnect.walletconnectv2.core.model.type

internal interface SettlementSequence<T : ClientParams> : SerializableJsonRpc {
    val id: Long
    val method: String
    val jsonrpc: String
    val params: T
}