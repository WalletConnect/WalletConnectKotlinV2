package com.walletconnect.walletconnectv2.common.model.vo

import com.squareup.moshi.JsonClass

sealed class JsonRpcResponseVO {
    val jsonrpc: String = "2.0"

    @JsonClass(generateAdapter = true)
    data class JsonRpcResult(
        val id: Long,
        val result: String
    ) : JsonRpcResponseVO()

    @JsonClass(generateAdapter = true)
    data class JsonRpcError(
        val id: Long,
        val error: Error,
    ) : JsonRpcResponseVO()

    data class Error(
        val code: Long,
        val message: String,
    )
}