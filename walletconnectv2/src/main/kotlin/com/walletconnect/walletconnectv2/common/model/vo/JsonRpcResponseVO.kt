package com.walletconnect.walletconnectv2.common.model.vo

import com.squareup.moshi.JsonClass

internal sealed class JsonRpcResponseVO {

    @JsonClass(generateAdapter = true)
    data class JsonRpcResult(
        val id: Long,
        val jsonrpc: String = "2.0",
        val result: String
    ) : JsonRpcResponseVO()

    @JsonClass(generateAdapter = true)
    data class JsonRpcError(
        val id: Long,
        val jsonrpc: String = "2.0",
        val error: Error
    ) : JsonRpcResponseVO()

    data class Error(
        val code: Long,
        val message: String,
    )

//    @JsonClass(generateAdapter = true)
//    data class Acknowledgement(
//        val id: Long,
//        val jsonrpc: String = "2.0",
//        val result: Boolean = true
//    ) : JsonRpcResponseVO()
}