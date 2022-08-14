package com.walletconnect.sign.common.model.vo.clientsync.common

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AgreementPeer(
    @Json(name = "publicKey")
    val publicKey: String,
)