package com.walletconnect.chat.common.json_rpc

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.android.internal.common.model.type.ClientParams
import com.walletconnect.chat.common.model.Media

internal sealed class ChatParams : ClientParams {

    @JsonClass(generateAdapter = true)
    internal data class InviteParams(
        @Json(name = "message")
        val message: String,
        @Json(name = "account")
        val account: String,
        @Json(name = "publicKey")
        val publicKey: String,
        @Json(name = "signature")
        val signature: String?,
    ) : ChatParams()

    @JsonClass(generateAdapter = true)
    internal data class MessageParams(
        @Json(name = "message")
        val message: String,
        @Json(name = "authorAccount")
        val authorAccount: String,
        @Json(name = "timestamp")
        val timestamp: Long,
        @Json(name = "media")
        val media: Media?,
    ) : ChatParams()

    @Suppress("CanSealedSubClassBeObject")
    internal class PingParams : ChatParams()

    @Suppress("CanSealedSubClassBeObject")
    internal class LeaveParams : ChatParams()
}
