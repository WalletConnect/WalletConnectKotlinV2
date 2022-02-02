package com.walletconnect.walletconnectv2.core.model.vo

import com.squareup.moshi.JsonClass

internal sealed class JsonRpcResponseVO {
    abstract val id: Long

    @JsonClass(generateAdapter = true)
    internal data class JsonRpcResult(
        override val id: Long,
        val jsonrpc: String = "2.0",
        val result: String
    ) : JsonRpcResponseVO()

    @JsonClass(generateAdapter = true)
    internal data class JsonRpcError(
        override val id: Long,
        val jsonrpc: String = "2.0",
        val error: Error
    ) : JsonRpcResponseVO()

    internal data class Error(
        val code: Long,
        val message: String,
    )
}