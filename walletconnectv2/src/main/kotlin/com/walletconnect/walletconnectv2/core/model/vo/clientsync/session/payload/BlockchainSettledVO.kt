package com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.payload

import com.squareup.moshi.Json

data class BlockchainSettledVO(
    @Json(name = "chains")
    val chains: List<String>,
    @Json(name = "accounts")
    val accounts: List<String>,
    //    @Json(name = "auth")
//    val auth: String?,
//    @Json(name = "signatures")
//    val signatures: List<String>?,
)