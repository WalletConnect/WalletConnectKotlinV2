package com.walletconnect.android_core.json_rpc.model

import com.squareup.moshi.JsonClass
import com.walletconnect.android_core.common.model.type.SerializableJsonRpc

sealed class JsonRpc {

    sealed class JsonRpcResponse : JsonRpc(), SerializableJsonRpc {
        abstract val id: Long

        @JsonClass(generateAdapter = false)
        data class JsonRpcResult(
            override val id: Long,
            val jsonrpc: String = "2.0",
            val result: Any,
        ) : JsonRpcResponse()

        @JsonClass(generateAdapter = true)
        data class JsonRpcError(
            override val id: Long,
            val jsonrpc: String = "2.0",
            val error: Error,
        ) : JsonRpcResponse()

        data class Error(
            val code: Int,
            val message: String,
        )
    }

    internal data class ClientJsonRpc(
        val id: Long,
        val jsonrpc: String,
        val method: String
    ) : JsonRpc()
}
