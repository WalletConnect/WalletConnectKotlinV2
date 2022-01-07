package com.walletconnect.walletconnectv2.relay.model.clientsync.session.before.proposal

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AppMetaData(
    @Json(name = "name")
    val name: String = "Peer",
    @Json(name = "description")
    val description: String = "",
    @Json(name = "url")
    val url: String = "",
    @Json(name = "icons")
    val icons: List<String> = emptyList()
)
