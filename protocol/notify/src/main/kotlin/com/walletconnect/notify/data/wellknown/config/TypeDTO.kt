@file:JvmSynthetic

package com.walletconnect.notify.data.wellknown.config

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class TypeDTO(
    @Json(name = "name")
    val name: String,
    @Json(name = "description")
    val description: String
)