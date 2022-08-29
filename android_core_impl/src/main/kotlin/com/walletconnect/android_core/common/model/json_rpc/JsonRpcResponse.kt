package com.walletconnect.android_core.common.model.json_rpc

import com.squareup.moshi.JsonClass

sealed class JsonRpcResponse {
    abstract val id: Long

    @JsonClass(generateAdapter = true)
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
    ) : JsonRpcResponse() {
        val errorMessage: String = "${error.message} : code: ${error.code}"
    }

    data class Error(
        val code: Int,
        val message: String,
    )
}