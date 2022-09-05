package com.walletconnect.android.impl.common.model.sync

data class ClientJsonRpc(
    val id: Long,
    val jsonrpc: String,
    val method: String
)