package com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.before.proposal

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class SessionProposerVO(
    @Json(name = "publicKey")
    val publicKey: String,
    @Json(name = "controller")
    val controller: Boolean,
    @Json(name = "metadata")
    val metadata: AppMetaDataVO?
)