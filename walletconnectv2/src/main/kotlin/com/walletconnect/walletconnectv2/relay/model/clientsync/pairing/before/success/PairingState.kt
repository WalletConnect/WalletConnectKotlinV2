package com.walletconnect.walletconnectv2.relay.model.clientsync.pairing.before.success

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.walletconnectv2.relay.model.clientsync.session.before.proposal.AppMetaData

@JsonClass(generateAdapter = true)
data class PairingState(
    @Json(name = "metadata")
    val metadata: AppMetaData? = null
)
