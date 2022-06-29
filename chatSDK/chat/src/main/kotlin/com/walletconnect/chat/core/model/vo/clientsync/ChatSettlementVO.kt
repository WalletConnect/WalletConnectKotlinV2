package com.walletconnect.chat.core.model.vo.clientsync

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.chat.copiedFromSign.core.model.type.JsonRpcClientSync
import com.walletconnect.chat.copiedFromSign.json_rpc.model.JsonRpcMethod
import com.walletconnect.chat.core.model.vo.clientsync.params.ChatParamsVO

internal sealed class ChatSettlementVO : JsonRpcClientSync<ChatParamsVO> {
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
    ) : ChatSettlementVO()

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
    ) : ChatSettlementVO()

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
    ) : ChatSettlementVO()

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
    ) : ChatSettlementVO()
}