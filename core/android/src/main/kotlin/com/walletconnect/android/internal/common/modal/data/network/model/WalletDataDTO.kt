package com.walletconnect.android.internal.common.modal.data.network.model

import com.squareup.moshi.Json

class WalletDataDTO(
    @Json(name = "id")
    val id: String,
    @Json(name = "android_app_id")
    val appId: String?,
)
