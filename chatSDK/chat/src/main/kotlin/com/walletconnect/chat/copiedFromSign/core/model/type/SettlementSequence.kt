package com.walletconnect.chat.copiedFromSign.core.model.type

internal interface SettlementSequence<T : ClientParams> : SerializableJsonRpc {
    val id: Long
    val method: String
    val jsonrpc: String
    val params: T
}