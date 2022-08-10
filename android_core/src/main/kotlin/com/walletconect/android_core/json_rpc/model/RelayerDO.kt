package com.walletconect.android_core.json_rpc.model

import com.squareup.moshi.JsonClass
import com.walletconect.android_core.common.model.type.SerializableJsonRpc

internal sealed class RelayerDO {

    internal sealed class JsonRpcResponse : RelayerDO(), SerializableJsonRpc {
        abstract val id: Long

        @JsonClass(generateAdapter = false)
        internal data class JsonRpcResult(
            override val id: Long,
            val jsonrpc: String = "2.0",
            val result: Any,
        ) : JsonRpcResponse()

        @JsonClass(generateAdapter = true)
        internal data class JsonRpcError(
            override val id: Long,
            val jsonrpc: String = "2.0",
            val error: Error,
        ) : JsonRpcResponse()

        internal data class Error(
            val code: Int,
            val message: String,
        )
    }

    internal data class ClientJsonRpc(
        val id: Long,
        val jsonrpc: String,
        val method: String
    ) : RelayerDO()
}
