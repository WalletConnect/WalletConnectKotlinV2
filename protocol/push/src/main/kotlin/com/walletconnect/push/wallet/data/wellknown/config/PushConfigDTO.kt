package com.walletconnect.push.wallet.data.wellknown.config


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PushConfigDTO(
    @Json(name = "version")
    val version: Int,
    @Json(name = "lastModified")
    val lastModified: Long,
    @Json(name = "types")
    val types: List<TypeDTO>
)