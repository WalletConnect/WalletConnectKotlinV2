package com.walletconnect.chat.common.json_rpc

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.android.internal.common.model.JsonRpcClientSync
import com.walletconnect.chat.json_rpc.model.JsonRpcMethod

internal sealed class ChatRpc : JsonRpcClientSync<ChatParams> {
    abstract override val id: Long
    abstract override val method: String
    abstract override val jsonrpc: String
    abstract override val params: ChatParams

    @JsonClass(generateAdapter = true)
    internal data class ChatInvite(
        @Json(name = "id")
        override val id: Long,
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_CHAT_INVITE,
        @Json(name = "params")
        override val params: ChatParams.InviteParams,
    ) : ChatRpc()

    @JsonClass(generateAdapter = true)
    internal data class ChatMessage(
        @Json(name = "id")
        override val id: Long,
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_CHAT_MESSAGE,
        @Json(name = "params")
        override val params: ChatParams.MessageParams,
    ) : ChatRpc()

    @JsonClass(generateAdapter = true)
    internal data class ChatPing(
        @Json(name = "id")
        override val id: Long,
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_CHAT_PING,
        @Json(name = "params")
        override val params: ChatParams.PingParams,
    ) : ChatRpc()

    @JsonClass(generateAdapter = true)
    internal data class ChatLeave(
        @Json(name = "id")
        override val id: Long,
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_CHAT_LEAVE,
        @Json(name = "params")
        override val params: ChatParams.LeaveParams,
    ) : ChatRpc()
}