package com.walletconnect.chat.core.model.vo.clientsync

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.android.impl.common.model.type.JsonRpcClientSync
import com.walletconnect.chat.copiedFromSign.JsonRpcMethod
import com.walletconnect.chat.core.model.vo.clientsync.params.ChatParamsVO

internal sealed class ChatRpcVO : JsonRpcClientSync<ChatParamsVO> {
    abstract override val id: Long
    abstract override val method: String
    abstract override val jsonrpc: String
    abstract override val params: ChatParamsVO

    @JsonClass(generateAdapter = true)
    internal data class ChatInvite(
        @Json(name = "id")
        override val id: Long,
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_CHAT_INVITE,
        @Json(name = "params")
        override val params: ChatParamsVO.InviteParams,
    ) : ChatRpcVO()

    @JsonClass(generateAdapter = true)
    internal data class ChatMessage(
        @Json(name = "id")
        override val id: Long,
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_CHAT_MESSAGE,
        @Json(name = "params")
        override val params: ChatParamsVO.MessageParams,
    ) : ChatRpcVO()

    @JsonClass(generateAdapter = true)
    internal data class ChatPing(
        @Json(name = "id")
        override val id: Long,
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_CHAT_PING,
        @Json(name = "params")
        override val params: ChatParamsVO.PingParams,
    ) : ChatRpcVO()

    @JsonClass(generateAdapter = true)
    internal data class ChatLeave(
        @Json(name = "id")
        override val id: Long,
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_CHAT_LEAVE,
        @Json(name = "params")
        override val params: ChatParamsVO.LeaveParams,
    ) : ChatRpcVO()
}