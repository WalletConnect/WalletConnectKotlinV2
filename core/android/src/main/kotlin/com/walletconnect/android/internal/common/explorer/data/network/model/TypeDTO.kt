@file:JvmSynthetic

package com.walletconnect.android.internal.common.explorer.data.network.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class TypeDTO(
    @Json(name = "name")
    val name: String,
    @Json(name = "id")
    val id: String,
    @Json(name = "description")
    val description: String,
)