@file:JvmSynthetic

package com.walletconnect.notify.data.jwt.subscriptionsChanged

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.notify.data.jwt.NotifyJwtBase

@JsonClass(generateAdapter = true)
internal data class SubscriptionsChangedResponseJwtClaim(
    @Json(name = "iss") override val issuer: String,
    @Json(name = "sub") val subject: String,
    @Json(name = "aud") val audience: String,
    @Json(name = "iat") override val issuedAt: Long,
    @Json(name = "ksu") val keyserverUrl: String,
    @Json(name = "exp") override val expiration: Long,
    @Json(name = "act") override val action: String = ACTION_CLAIM_VALUE,
) : NotifyJwtBase {
    override val requiredActionValue: String = ACTION_CLAIM_VALUE
}

private const val ACTION_CLAIM_VALUE = "notify_subscriptions_changed_response"