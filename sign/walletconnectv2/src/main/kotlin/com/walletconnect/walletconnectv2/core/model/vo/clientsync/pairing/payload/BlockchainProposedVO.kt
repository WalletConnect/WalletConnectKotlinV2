package com.walletconnect.walletconnectv2.core.model.vo.clientsync.pairing.payload

import com.squareup.moshi.Json

data class BlockchainProposedVO(
    @Json(name = "chains")
    val chains: List<String>
)