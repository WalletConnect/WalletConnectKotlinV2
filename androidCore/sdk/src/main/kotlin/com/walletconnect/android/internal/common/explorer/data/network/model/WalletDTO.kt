package com.walletconnect.android.internal.common.explorer.data.network.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WalletDTO(
    @Json(name = "id")
    val id: String,
    @Json(name = "name")
    val name: String,
    @Json(name = "description")
    val description: String?,
    @Json(name = "homepage")
    val homePage: String,
    @Json(name = "image_id")
    val imageId: String,
    @Json(name = "mobile")
    val mobile: MobileDTO,
    @Json(name = "app")
    val app: AppDTO,
)
