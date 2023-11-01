@file:JvmSynthetic

package com.walletconnect.notify.data.jwt.update

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.notify.data.jwt.NotifyJwtBase

@JsonClass(generateAdapter = true)
internal data class UpdateRequestJwtClaim(
    @Json(name = "iss") override val issuer: String,
    @Json(name = "sub") val subject: String,
    @Json(name = "aud") val audience: String,
    @Json(name = "iat") override val issuedAt: Long,
    @Json(name = "exp") override val expiration: Long,
    @Json(name = "ksu") val keyserverUrl: String,
    @Json(name = "app") val app: String,
    @Json(name = "scp") val scope: String,
    @Json(name = "act") override val action: String = ACTION_CLAIM_VALUE,
) : NotifyJwtBase {
    override val requiredActionValue: String = ACTION_CLAIM_VALUE
}

private const val ACTION_CLAIM_VALUE = "notify_update"