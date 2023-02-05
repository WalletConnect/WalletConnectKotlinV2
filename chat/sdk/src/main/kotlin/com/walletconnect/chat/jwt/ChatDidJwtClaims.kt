@file:JvmSynthetic

package com.walletconnect.chat.jwt

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.chat.common.model.Media
import com.walletconnect.foundation.util.jwt.JwtClaims

internal sealed interface ChatDidJwtClaims : JwtClaims {
    val issuer: String

    @JsonClass(generateAdapter = true)
    data class InviteKey(
        @Json(name = "iss") override val issuer: String,
        @Json(name = "sub") val subject: String,
        @Json(name = "aud") val audience: String,
        @Json(name = "iat") val issuedAt: Long,
        @Json(name = "exp") val expiration: Long,
        @Json(name = "pkh") val pkh: String,
    ) : ChatDidJwtClaims

    @JsonClass(generateAdapter = true)
    data class InviteProposal(
        @Json(name = "iss") override val issuer: String,
        @Json(name = "iat") val issuedAt: Long,
        @Json(name = "exp") val expiration: Long,
        @Json(name = "ksu") val keyserverUrl: String,

        @Json(name = "aud") val audience: String, // responder/invitee blockchain account (did:pkh)
        @Json(name = "sub") val subject: String, // opening message included in the invite
        @Json(name = "pke") val inviterPublicKey: String, // proposer/inviter public key (did:key)
    ) : ChatDidJwtClaims

    @JsonClass(generateAdapter = true)
    data class InviteApproval(
        @Json(name = "iss") override val issuer: String,
        @Json(name = "iat") val issuedAt: Long,
        @Json(name = "exp") val expiration: Long,
        @Json(name = "ksu") val keyserverUrl: String,

        @Json(name = "aud") val audience: String, // proposer/inviter blockchain account (did:pkh)
        @Json(name = "sub") val subject: String, // public key sent by the responder/invitee
    ) : ChatDidJwtClaims

    @JsonClass(generateAdapter = true)
    data class ChatMessage(
        @Json(name = "iss") override val issuer: String,
        @Json(name = "iat") val issuedAt: Long,
        @Json(name = "exp") val expiration: Long,
        @Json(name = "ksu") val keyserverUrl: String,

        @Json(name = "aud") val audience: String, // recipient blockchain account (did:pkh)
        @Json(name = "sub") val subject: String, // message sent by the author account
        @Json(name = "xma") val media: Media?, // extensible media attachment (optional)
    ) : ChatDidJwtClaims

    @JsonClass(generateAdapter = true)
    data class ChatReceipt(
        @Json(name = "iss") override val issuer: String,
        @Json(name = "iat") val issuedAt: Long,
        @Json(name = "exp") val expiration: Long,
        @Json(name = "ksu") val keyserverUrl: String,

        @Json(name = "aud") val audience: String, // sender blockchain account (did:pkh)
        @Json(name = "sub") val subject: String, // hash of the message received
    ) : ChatDidJwtClaims
}