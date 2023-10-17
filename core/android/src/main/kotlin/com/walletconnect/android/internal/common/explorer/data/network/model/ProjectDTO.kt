package com.walletconnect.android.internal.common.explorer.data.network.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ProjectDTO(
    @Json(name = "id")
    val id: String,
    @Json(name = "name")
    val name: String?,
    @Json(name = "homepage")
    val homepage: String?,
    @Json(name = "image_id")
    val imageId: String?,
    @Json(name = "description")
    val description: String?,
    @Json(name = "image_url")
    val imageUrl: ImageUrlDTO?,
    @Json(name = "dapp_url")
    val dappUrl: String?,
)
//todo Add is_verified