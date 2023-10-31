@file:JvmSynthetic

package com.walletconnect.notify.data.jwt.message

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.notify.data.jwt.NotifyJwtBase

@JsonClass(generateAdapter = true)
internal data class MessageRequestJwtClaim(
    @Json(name = "iss") override val issuer: String,
    @Json(name = "sub") val subject: String,
    @Json(name = "iat") override val issuedAt: Long,
    @Json(name = "exp") override val expiration: Long,
    @Json(name = "app") val app: String,
    @Json(name = "msg") val message: Message,
    @Json(name = "act") override val action: String = ACTION_CLAIM_VALUE,
) : NotifyJwtBase {
    override val requiredActionValue: String = ACTION_CLAIM_VALUE

    @JsonClass(generateAdapter = true)
    data class Message(
        @Json(name = "title") val title: String,
        @Json(name = "body") val body: String,
        @Json(name = "icon") val icon: String,
        @Json(name = "url") val url: String,
        @Json(name = "type") val type: String,
    )

}

private const val ACTION_CLAIM_VALUE = "notify_message"

