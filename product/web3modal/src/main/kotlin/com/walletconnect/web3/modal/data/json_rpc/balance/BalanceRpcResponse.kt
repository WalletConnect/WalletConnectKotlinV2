package com.walletconnect.web3.modal.data.json_rpc.balance

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

internal data class BalanceRpcResponse(
    @Json(name = "id")
    val id: Long,
    @Json(name = "jsonrpc")
    val jsonrpc: String = "2.0",
    @Json(name = "result")
    val result: String?,
    @Json(name = "error")
    val error: Error?,
)

@JsonClass(generateAdapter = true)
internal data class Error(
    val code: Int,
    val message: String,
)