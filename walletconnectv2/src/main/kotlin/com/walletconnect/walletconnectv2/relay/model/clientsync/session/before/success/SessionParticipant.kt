package com.walletconnect.walletconnectv2.relay.model.clientsync.session.before.success

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.walletconnectv2.common.model.AppMetaData

@JsonClass(generateAdapter = true)
data class SessionParticipant(
    @Json(name = "publicKey")
    val publicKey: String,
    @Json(name = "metadata")
    val metadata: AppMetaData? = null
)
