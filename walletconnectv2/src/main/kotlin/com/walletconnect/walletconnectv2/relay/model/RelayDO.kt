package com.walletconnect.walletconnectv2.relay.model

import com.squareup.moshi.JsonClass
import com.walletconnect.walletconnectv2.core.model.type.ClientSyncJsonRpc

internal sealed class RelayDO {

    internal sealed class JsonRpcResponse : RelayDO(), ClientSyncJsonRpc {

        abstract override val id: Long

        @JsonClass(generateAdapter = true)
        internal data class JsonRpcResult(
            override val id: Long,
            val jsonrpc: String = "2.0",
            val result: Any
        ) : JsonRpcResponse()

        @JsonClass(generateAdapter = true)
        internal data class JsonRpcError(
            override val id: Long,
            val jsonrpc: String = "2.0",
            val error: Error
        ) : JsonRpcResponse() {
            val code: Long = error.code
            val message: String = error.message
        }

        internal data class Error(
            val code: Long,
            val message: String,
        )
    }

    internal data class ClientJsonRpc(
        val id: Long,
        val jsonrpc: String,
        val method: String
    ) : RelayDO()
}
