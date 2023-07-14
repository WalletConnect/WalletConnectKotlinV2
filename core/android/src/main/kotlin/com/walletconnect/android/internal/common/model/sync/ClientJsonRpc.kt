package com.walletconnect.android.internal.common.model.sync

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ClientJsonRpc(
    val id: Long,
    val jsonrpc: String,
    val method: String
)