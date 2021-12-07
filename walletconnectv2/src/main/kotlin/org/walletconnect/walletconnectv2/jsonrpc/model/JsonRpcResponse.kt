package org.walletconnect.walletconnectv2.jsonrpc.model

import com.squareup.moshi.JsonClass
import org.walletconnect.walletconnectv2.clientsync.ClientSyncJsonRpc

sealed class JsonRpcResponse : ClientSyncJsonRpc{

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