package org.walletconnect.walletconnectv2.relay.data.model

import com.squareup.moshi.Json

data class Request(
    @Json(name = "id")
    val id: Long,
    @Json(name = "jsonrpc")
    val jsonrpc: String = "2.0",
    @Json(name = "method")
    val method: String
)
