package com.walletconnect.sign.common.model.vo.clientsync.session.payload

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class JsonRpcVO(
    @Json(name = "methods")
    val methods: List<String>
)
