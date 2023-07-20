package com.walletconnect.chat.common.json_rpc

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.android.internal.common.model.type.ClientParams

internal sealed class ChatParams : ClientParams {

    @JsonClass(generateAdapter = true)
    internal data class InviteParams(
        @Json(name = "inviteAuth")
        val inviteAuth: String,
    ) : ChatParams()

    @JsonClass(generateAdapter = true)
    internal data class MessageParams(
        @Json(name = "messageAuth")
        val messageAuth: String,
    ) : ChatParams()

    @Suppress("CanSealedSubClassBeObject")
    internal class PingParams : ChatParams()

    @Suppress("CanSealedSubClassBeObject")
    internal class LeaveParams : ChatParams()
}
