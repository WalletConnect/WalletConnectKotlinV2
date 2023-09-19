@file:JvmSynthetic

package com.walletconnect.notify.data.wellknown.config

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.android.internal.common.model.AppMetaData

@JsonClass(generateAdapter = true)
internal data class NotifyConfigDTO(
    @Json(name = "schemaVersion")
    val schemaVersion: Int,
    @Json(name = "types")
    val types: List<TypeDTO>,
    @Json(name = "name")
    val name: String,
    @Json(name = "description")
    val description: String,
    @Json(name = "icons")
    val icons: List<String>
)