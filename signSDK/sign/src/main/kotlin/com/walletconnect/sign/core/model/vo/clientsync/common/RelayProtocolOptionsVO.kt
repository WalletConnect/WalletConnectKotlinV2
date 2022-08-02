package com.walletconnect.sign.core.model.vo.clientsync.common

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RelayProtocolOptionsVO(
    val protocol: String = "irn",
    val data: String? = null
)