package com.walletconnect.android.internal.common.explorer.data.network.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class InjectedDTO(
    @Json(name = "namespace")
    val namespace: String,
    @Json(name = "injected_id")
    val injectedId: String
)