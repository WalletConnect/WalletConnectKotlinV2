package com.walletconnect.walletconnectv2.core.model.vo.jsonRpc

import com.squareup.moshi.JsonClass

internal sealed class JsonRpcResponseVO {
    abstract val id: Long

    @JsonClass(generateAdapter = true)
    internal data class JsonRpcResult(
        override val id: Long,
        val jsonrpc: String = "2.0",
        val result: Any, //todo: should be string???
    ) : JsonRpcResponseVO()

    @JsonClass(generateAdapter = true)
    internal data class JsonRpcError(
        override val id: Long,
        val jsonrpc: String = "2.0",
        val error: Error
    ) : JsonRpcResponseVO() {
        val errorMessage: String = "${error.message} : code: ${error.code}"
    }

    internal data class Error(
        val code: Int,
        val message: String,
    )
}