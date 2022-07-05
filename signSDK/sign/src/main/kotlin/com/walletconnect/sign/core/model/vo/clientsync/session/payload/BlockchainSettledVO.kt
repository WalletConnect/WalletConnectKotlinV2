package com.walletconnect.sign.core.model.vo.clientsync.session.payload

import com.squareup.moshi.Json

data class BlockchainSettledVO(
    @Json(name = "accounts")
    val accounts: List<String>,
    @Json(name = "chains")
    val chains: List<String>,
)