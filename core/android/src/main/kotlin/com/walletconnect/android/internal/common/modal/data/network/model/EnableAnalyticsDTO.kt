package com.walletconnect.android.internal.common.modal.data.network.model

import com.squareup.moshi.Json

data class EnableAnalyticsDTO(
    @Json(name = "isAnalyticsEnabled")
    val isAnalyticsEnabled: Boolean
)
