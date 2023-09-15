package com.walletconnect.android.internal.common.modal.data.network.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WalletDTO(
    @Json(name = "id")
    val id: String,
    @Json(name = "name")
    val name: String,
    @Json(name = "homepage")
    val homePage: String,
    @Json(name = "image_id")
    val imageId: String,
    @Json(name = "order")
    val order: String,
    @Json(name = "mobile_link")
    val mobileLink: String?,
    @Json(name = "desktop_link")
    val desktopLink: String?,
    @Json(name = "webapp_link")
    val webappLink: String?,
    @Json(name = "app_store")
    val appStore: String?,
    @Json(name = "play_store")
    val playStore: String?,
)
