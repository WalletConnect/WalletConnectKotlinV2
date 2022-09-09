package com.walletconnect.chat.core.model.vo.clientsync.params

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.chat.copiedFromSign.core.model.type.ClientParams
import com.walletconnect.chat.core.model.vo.MediaVO

internal sealed class ChatParamsVO : ClientParams {

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
    ) : ChatParamsVO()

    @JsonClass(generateAdapter = true)
    internal data class AcceptanceParams(
        @Json(name = "publicKey")
        val publicKey: String,
    ) : ChatParamsVO()

    @JsonClass(generateAdapter = true)
    internal data class MessageParams(
        @Json(name = "message")
        val message: String,
        @Json(name = "authorAccount")
        val authorAccount: String,
        @Json(name = "timestamp")
        val timestamp: Long,
        @Json(name = "media")
        val media: MediaVO?,
    ) : ChatParamsVO()

    @Suppress("CanSealedSubClassBeObject")
    internal class PingParams : ChatParamsVO()

    @Suppress("CanSealedSubClassBeObject")
    internal class LeaveParams : ChatParamsVO()
}
