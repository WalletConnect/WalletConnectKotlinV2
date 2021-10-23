package org.walletconnect.walletconnectv2.clientcomm.session.proposal

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SessionProposer(
    @Json(name = "publicKey")
    val publicKey: String,
    @Json(name = "controller")
    val controller: Boolean,
    @Json(name = "metadata")
    val metadata: AppMetaData?
)
