package com.walletconnect.notify.common.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class ServerNotification(
    val id: String,
    @Json(name = "sent_at") val sentAt: Long,
    val type: String,
    val title: String,
    val body: String,
    val icon: String?,
    val url: String?,
)