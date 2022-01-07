package com.walletconnect.walletconnectv2.common.model

import com.squareup.moshi.JsonClass
import com.walletconnect.walletconnectv2.relay.model.clientsync.ClientSyncJsonRpc

//TODO: Create for every layer
sealed class JsonRpcResponse : ClientSyncJsonRpc {

    abstract override val id: Long
    val jsonrpc: String = "2.0"

    @JsonClass(generateAdapter = true)
    data class JsonRpcResult(
        override val id: Long,
        val result: String
    ) : JsonRpcResponse()

    @JsonClass(generateAdapter = true)
    data class JsonRpcError(
        override val id: Long,
        val error: Error,
    ) : JsonRpcResponse()

    data class Error(
        val code: Long,
        val message: String,
    )
}