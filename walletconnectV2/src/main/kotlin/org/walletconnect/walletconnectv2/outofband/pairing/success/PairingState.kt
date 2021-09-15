package org.walletconnect.walletconnectv2.outofband.pairing.success

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PairingState(
    @Json(name = "metadata")
    val metadata: AppMetaData? = null
)
