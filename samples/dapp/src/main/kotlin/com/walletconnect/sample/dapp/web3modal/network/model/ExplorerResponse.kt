package com.walletconnect.sample.dapp.web3modal.network.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.sample.dapp.web3modal.data.explorer.MapToList

@JsonClass(generateAdapter = true)
data class ExplorerResponse(
    @Json(name = "listings")
    @MapToList
    val wallets: List<ExplorerWalletResponse>,
    @Json(name = "count")
    val count: Int,
    @Json(name = "total")
    val total: Int
)
