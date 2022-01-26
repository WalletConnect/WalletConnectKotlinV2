package com.walletconnect.walletconnectv2.common.model.vo.clientsync.session.before.proposal

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class AppMetaDataVO(
    @Json(name = "name")
    val name: String,
    @Json(name = "description")
    val description: String,
    @Json(name = "url")
    val url: String,
    @Json(name = "icons")
    val icons: List<String>
)