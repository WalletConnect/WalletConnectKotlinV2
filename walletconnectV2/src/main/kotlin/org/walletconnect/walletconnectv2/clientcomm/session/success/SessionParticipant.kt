package org.walletconnect.walletconnectv2.clientcomm.session.success

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.walletconnect.walletconnectv2.clientcomm.session.proposal.AppMetaData

@JsonClass(generateAdapter = true)
data class SessionParticipant(
    @Json(name = "publicKey")
    val publicKey: String,
    @Json(name = "metadata")
    val metadata: AppMetaData? = null
)
