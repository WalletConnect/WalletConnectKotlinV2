@file:JvmSynthetic

package com.walletconnect.notify.data.jwt.subscriptionsChanged

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.notify.common.model.ServerSubscription
import com.walletconnect.notify.data.jwt.NotifyJwtBase

@JsonClass(generateAdapter = true)
internal data class SubscriptionsChangedRequestJwtClaim(
    @Json(name = "iss") override val issuer: String,
    @Json(name = "sub") val subject: String,
    @Json(name = "aud") val audience: String,
    @Json(name = "iat") override val issuedAt: Long,
    @Json(name = "exp") override val expiration: Long,
    @Json(name = "sbs") val subscriptions: List<ServerSubscription>,
    @Json(name = "act") override val action: String = "notify_subscriptions_changed",
): NotifyJwtBase