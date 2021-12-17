package com.walletconnect.walletconnectv2.clientsync.pairing.before.success

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.walletconnectv2.common.AppMetaData

@JsonClass(generateAdapter = true)
data class PairingState(
    @Json(name = "metadata")
    val metadata: AppMetaData? = null
)
