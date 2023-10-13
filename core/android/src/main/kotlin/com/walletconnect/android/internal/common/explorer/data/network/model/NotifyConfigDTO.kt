@file:JvmSynthetic

package com.walletconnect.android.internal.common.explorer.data.network.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NotifyConfigDTO(
    @Json(name = "schemaVersion")
    val schemaVersion: Int,
    @Json(name = "types")
    val types: List<NotificationTypeDTO>,
    @Json(name = "name")
    val name: String,
    @Json(name = "description")
    val description: String,
    @Json(name = "icons")
    val icons: List<String>,
)