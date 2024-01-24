package com.walletconnect.notify.data.jwt.getNotifications

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.notify.common.model.ServerNotification
import com.walletconnect.notify.data.jwt.NotifyJwtBase

@JsonClass(generateAdapter = true)
internal data class GetNotificationsResponseJwtClaim(
    @Json(name = "iss") override val issuer: String,
    @Json(name = "sub") val subject: String,
    @Json(name = "aud") val audience: String,
    @Json(name = "iat") override val issuedAt: Long,
    @Json(name = "exp") override val expiration: Long,
    @Json(name = "act") override val action: String = ACTION_CLAIM_VALUE,
    @Json(name = "mjv") override val version: String = VERSION,
    @Json(name = "nfs") val notifications: List<ServerNotification>,
    @Json(name = "mre") val hasMore: Boolean,
) : NotifyJwtBase {
    override val requiredActionValue: String = ACTION_CLAIM_VALUE
    override val requiredVersionValue: String = VERSION
}

private const val ACTION_CLAIM_VALUE = "notify_get_notifications_response"
private const val VERSION = "1"