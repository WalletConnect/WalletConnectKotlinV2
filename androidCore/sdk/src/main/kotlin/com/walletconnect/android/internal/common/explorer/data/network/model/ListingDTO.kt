package com.walletconnect.android.internal.common.explorer.data.network.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ListingDTO(
    @Json(name = "id")
    val id: String,
    @Json(name = "name")
    val name: String,
    @Json(name = "description")
    val description: String?,
    @Json(name = "homepage")
    val homepage: String,
    @Json(name = "chains")
    val chains: List<String>,
    @Json(name = "versions")
    val versions: List<String>,
    @Json(name = "sdks")
    val sdks: List<String>,
    @Json(name = "app_type")
    val appType: String,
    @Json(name = "image_id")
    val imageId: String,
    @Json(name = "image_url")
    val imageUrl: ImageUrlDTO,
    @Json(name = "app")
    val app: AppDTO,
    @Json(name = "injected")
    val injected: List<InjectedDTO>?,
    @Json(name = "mobile")
    val mobile: MobileDTO,
    @Json(name = "desktop")
    val desktop: DesktopDTO,
    @Json(name = "supported_standards")
    val supportedStandards: List<SupportedStandardDTO>,
    @Json(name = "metadata")
    val metadata: MetadataDTO,
    @Json(name = "updatedAt")
    val updatedAt: String
)