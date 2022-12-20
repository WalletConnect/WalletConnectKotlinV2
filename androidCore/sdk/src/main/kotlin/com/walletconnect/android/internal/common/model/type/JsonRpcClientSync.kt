package com.walletconnect.android.internal.common.model.type

import com.walletconnect.android.internal.common.SerializableJsonRpc

interface JsonRpcClientSync<T : ClientParams> : SerializableJsonRpc {
    val id: Long
    val method: String
    val jsonrpc: String
    val params: T
}