package com.walletconnect.android.pulse.model.properties

import com.squareup.moshi.Json

data class SelectWalletProperties(
    @Json(name = "name")
    val name: String,
    @Json(name = "platform")
    val platform: String
)