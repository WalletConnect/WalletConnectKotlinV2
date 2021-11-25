package org.walletconnect.walletconnectv2.clientsync.session.after.params

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.walletconnect.walletconnectv2.util.defaultId

@JsonClass(generateAdapter = true)
data class Reason(
    @Json(name = "code")
    val code: Int = Int.defaultId,
    @Json(name = "message")
    val message: String
)