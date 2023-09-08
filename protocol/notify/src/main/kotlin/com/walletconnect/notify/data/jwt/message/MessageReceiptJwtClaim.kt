@file:JvmSynthetic

package com.walletconnect.notify.data.jwt.message

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.notify.data.jwt.NotifyJwtBase

@JsonClass(generateAdapter = true)
internal data class MessageReceiptJwtClaim(
    @Json(name = "iss") override val issuer: String,
    @Json(name = "aud") val audience: String,
    @Json(name = "iat") override val issuedAt: Long,
    @Json(name = "exp") override val expiration: Long,
    @Json(name = "ksu") val keyserverUrl: String,
    @Json(name = "sub") val subject: String,
    @Json(name = "app") val dappUrl: String,
    @Json(name = "act") override val action: String = "notify_receipt",
) : NotifyJwtBase