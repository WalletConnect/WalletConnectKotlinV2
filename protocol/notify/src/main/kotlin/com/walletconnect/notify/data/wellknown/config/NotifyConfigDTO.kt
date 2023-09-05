@file:JvmSynthetic

package com.walletconnect.notify.data.wellknown.config

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class NotifyConfigDTO(
    @Json(name = "version")
    val version: Int,
    @Json(name = "lastModified")
    val lastModified: Long,
    @Json(name = "types")
    val types: List<TypeDTO>
)