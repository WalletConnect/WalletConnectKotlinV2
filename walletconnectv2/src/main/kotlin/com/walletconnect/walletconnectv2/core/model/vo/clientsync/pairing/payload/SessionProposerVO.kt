package com.walletconnect.walletconnectv2.core.model.vo.clientsync.pairing.payload

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.common.AppMetaDataVO

@JsonClass(generateAdapter = true)
internal data class SessionProposerVO(
    @Json(name = "publicKey")
    val publicKey: String,
    @Json(name = "metadata")
    val metadata: AppMetaDataVO?
)