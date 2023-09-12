package com.walletconnect.web3.modal.data.network.model

import com.squareup.moshi.Json

internal data class GetWalletsDTO(
    @Json(name = "count")
    val count: Int,
    @Json(name = "data")
    val data: List<WalletDTO>,
)