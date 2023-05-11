package com.walletconnect.web3.modal.network.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.web3.modal.data.explorer.MapToList

@JsonClass(generateAdapter = true)
internal data class ExplorerResponse(
    @Json(name = "listings")
    @MapToList
    val wallets: List<ExplorerWalletResponse>,
    @Json(name = "count")
    val count: Int,
    @Json(name = "total")
    val total: Int
)
