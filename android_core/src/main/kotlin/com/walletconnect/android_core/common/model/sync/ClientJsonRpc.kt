package com.walletconnect.android_core.common.model.sync

data class ClientJsonRpc(
    val id: Long,
    val jsonrpc: String,
    val method: String
)