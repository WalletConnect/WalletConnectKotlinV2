package com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.payload

import com.squareup.moshi.Json

data class BlockchainSettledVO(
    @Json(name = "accounts")
    val accounts: List<String>,
)