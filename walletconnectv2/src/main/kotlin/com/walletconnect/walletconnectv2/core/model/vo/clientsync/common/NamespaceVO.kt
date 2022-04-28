package com.walletconnect.walletconnectv2.core.model.vo.clientsync.common

import com.squareup.moshi.Json

data class NamespaceVO(
    @Json(name = "chains")
    val chains: List<String>,
    @Json(name = "methods")
    val methods: List<String>,
    @Json(name = "events")
    val events: List<String>,
)