package com.walletconnect.android.impl.common.model.type

import com.walletconnect.android.common.SerializableJsonRpc

interface JsonRpcClientSync<T : ClientParams> : SerializableJsonRpc {
    val id: Long
    val method: String
    val jsonrpc: String
    val params: T
}