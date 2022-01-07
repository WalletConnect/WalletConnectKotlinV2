package com.walletconnect.walletconnectv2.common.model.vo

import com.squareup.moshi.JsonClass
import com.walletconnect.walletconnectv2.relay.model.clientsync.ClientSyncJsonRpc

sealed class JsonRpcResponseVO : ClientSyncJsonRpc {

    abstract override val id: Long
    val jsonrpc: String = "2.0"

    @JsonClass(generateAdapter = true)
    data class JsonRpcResult(
        override val id: Long,
        val result: String
    ) : JsonRpcResponseVO()

    @JsonClass(generateAdapter = true)
    data class JsonRpcError(
        override val id: Long,
        val error: Error,
    ) : JsonRpcResponseVO()

    data class Error(
        val code: Long,
        val message: String,
    )
}