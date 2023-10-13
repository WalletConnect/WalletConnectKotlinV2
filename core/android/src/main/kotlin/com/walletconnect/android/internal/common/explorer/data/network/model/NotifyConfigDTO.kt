@file:JvmSynthetic

package com.walletconnect.android.internal.common.explorer.data.network.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class NotifyConfigDTO(
    @Json(name = "data")
    val data: NotifyConfigDataDTO,
)

@JsonClass(generateAdapter = true)
data class NotifyConfigDataDTO(
    @Json(name = "name") val name: String,
    @Json(name = "homepage") val homepage: String,
    @Json(name = "description") val description: String,
    @Json(name = "dapp_url") val dappUrl: String,
    @Json(name = "image_url") val imageUrl: ImageUrlDTO,
    @Json(name = "notificationTypes") val notificationTypes: List<NotificationTypeDTO>,
    @Json(name = "isVerified") val isVerified: Boolean,
)

@JsonClass(generateAdapter = true)
data class NotificationTypeDTO(
    @Json(name = "name")
    val name: String,
    @Json(name = "id")
    val id: String,
    @Json(name = "description")
    val description: String,
)