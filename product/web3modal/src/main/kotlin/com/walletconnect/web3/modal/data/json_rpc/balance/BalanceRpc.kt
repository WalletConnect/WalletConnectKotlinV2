package com.walletconnect.web3.modal.data.json_rpc.balance

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.util.generateId
import com.walletconnect.web3.modal.data.json_rpc.JsonRpcMethod

internal data class BalanceRequest(
    val address: String,
    @Json(name = "id")
    val id: Long = generateId(),
    @Json(name = "jsonrpc")
    val jsonrpc: String = "2.0",
    @Json(name = "method")
    val method: String = JsonRpcMethod.ETH_GET_BALANCE,
    @Json(name = "params")
    val params: List<String> = listOf(address, "latest")
)
