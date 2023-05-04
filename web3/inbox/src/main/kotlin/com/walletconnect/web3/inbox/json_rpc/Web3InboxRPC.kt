package com.walletconnect.web3.inbox.json_rpc

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.android.internal.common.model.type.JsonRpcClientSync
import com.walletconnect.util.generateId

internal sealed interface Web3InboxRPC : JsonRpcClientSync<Web3InboxParams> {

    /**
     * [Request] represents communication from Javascript -> Kotlin
     */
    sealed interface Request : Web3InboxRPC {
        override val params: Web3InboxParams.Request

        sealed interface Chat : Request {
            @JsonClass(generateAdapter = true)
            data class Register(
                @Json(name = "id")
                override val id: Long,
                @Json(name = "jsonrpc")
                override val jsonrpc: String = "2.0",
                @Json(name = "method")
                override val method: String = Web3InboxMethods.Request.REGISTER,
                @Json(name = "params")
                override val params: Web3InboxParams.Request.Chat.RegisterParams,
            ) : Chat

            @JsonClass(generateAdapter = true)
            data class Resolve(
                @Json(name = "id")
                override val id: Long,
                @Json(name = "jsonrpc")
                override val jsonrpc: String = "2.0",
                @Json(name = "method")
                override val method: String = Web3InboxMethods.Request.RESOLVE,
                @Json(name = "params")
                override val params: Web3InboxParams.Request.Chat.ResolveParams,
            ) : Chat


            @JsonClass(generateAdapter = true)
            data class Accept(
                @Json(name = "id")
                override val id: Long,
                @Json(name = "jsonrpc")
                override val jsonrpc: String = "2.0",
                @Json(name = "method")
                override val method: String = Web3InboxMethods.Request.ACCEPT,
                @Json(name = "params")
                override val params: Web3InboxParams.Request.Chat.AcceptParams,
            ) : Chat

            @JsonClass(generateAdapter = true)
            data class Reject(
                @Json(name = "id")
                override val id: Long,
                @Json(name = "jsonrpc")
                override val jsonrpc: String = "2.0",
                @Json(name = "method")
                override val method: String = Web3InboxMethods.Request.REJECT,
                @Json(name = "params")
                override val params: Web3InboxParams.Request.Chat.RejectParams,
            ) : Chat

            @JsonClass(generateAdapter = true)
            data class Invite(
                @Json(name = "id")
                override val id: Long,
                @Json(name = "jsonrpc")
                override val jsonrpc: String = "2.0",
                @Json(name = "method")
                override val method: String = Web3InboxMethods.Request.INVITE,
                @Json(name = "params")
                override val params: Web3InboxParams.Request.Chat.InviteParams,
            ) : Chat

            @JsonClass(generateAdapter = true)
            data class GetReceivedInvites(
                @Json(name = "id")
                override val id: Long,
                @Json(name = "jsonrpc")
                override val jsonrpc: String = "2.0",
                @Json(name = "method")
                override val method: String = Web3InboxMethods.Request.GET_RECEIVED_INVITES,
                @Json(name = "params")
                override val params: Web3InboxParams.Request.Chat.GetReceivedInvitesParams,
            ) : Chat

            @JsonClass(generateAdapter = true)
            data class GetSentInvites(
                @Json(name = "id")
                override val id: Long,
                @Json(name = "jsonrpc")
                override val jsonrpc: String = "2.0",
                @Json(name = "method")
                override val method: String = Web3InboxMethods.Request.GET_SENT_INVITES,
                @Json(name = "params")
                override val params: Web3InboxParams.Request.Chat.GetSentInvitesParams,
            ) : Chat

            @JsonClass(generateAdapter = true)
            data class GetThreads(
                @Json(name = "id")
                override val id: Long,
                @Json(name = "jsonrpc")
                override val jsonrpc: String = "2.0",
                @Json(name = "method")
                override val method: String = Web3InboxMethods.Request.GET_THREADS,
                @Json(name = "params")
                override val params: Web3InboxParams.Request.Chat.GetThreadsParams,
            ) : Chat

            @JsonClass(generateAdapter = true)
            data class GetMessages(
                @Json(name = "id")
                override val id: Long,
                @Json(name = "jsonrpc")
                override val jsonrpc: String = "2.0",
                @Json(name = "method")
                override val method: String = Web3InboxMethods.Request.GET_MESSAGES,
                @Json(name = "params")
                override val params: Web3InboxParams.Request.Chat.GetMessagesParams,
            ) : Chat

            @JsonClass(generateAdapter = true)
            data class Message(
                @Json(name = "id")
                override val id: Long,
                @Json(name = "jsonrpc")
                override val jsonrpc: String = "2.0",
                @Json(name = "method")
                override val method: String = Web3InboxMethods.Request.MESSAGE,
                @Json(name = "params")
                override val params: Web3InboxParams.Request.Chat.MessageParams,
            ) : Chat
        }

        sealed interface Push : Request {
            @JsonClass(generateAdapter = true)
            data class GetActiveSubscriptions(
                @Json(name = "id")
                override val id: Long,
                @Json(name = "jsonrpc")
                override val jsonrpc: String = "2.0",
                @Json(name = "method")
                override val method: String = Web3InboxMethods.Request.GET_ACTIVE_SUBSCRIPTIONS,
                override val params: Web3InboxParams.Request.Empty,
            ) : Push
        }
    }

    /**
     * [Call] represents communication from Kotlin -> Javascript
     */
    sealed interface Call : Web3InboxRPC {
        override val params: Web3InboxParams.Call

        sealed interface Chat : Call {
            override val params: Web3InboxParams.Call.Chat

            @JsonClass(generateAdapter = true)
            data class Invite(
                @Json(name = "id")
                override val id: Long = generateId(),
                @Json(name = "jsonrpc")
                override val jsonrpc: String = "2.0",
                @Json(name = "method")
                override val method: String = Web3InboxMethods.Call.Chat.INVITE,
                @Json(name = "params")
                override val params: Web3InboxParams.Call.Chat.InviteParams,
            ) : Chat

            @JsonClass(generateAdapter = true)
            data class Message(
                @Json(name = "id")
                override val id: Long = generateId(),
                @Json(name = "jsonrpc")
                override val jsonrpc: String = "2.0",
                @Json(name = "method")
                override val method: String = Web3InboxMethods.Call.Chat.MESSAGE,
                @Json(name = "params")
                override val params: Web3InboxParams.Call.Chat.MessageParams,
            ) : Chat

            @JsonClass(generateAdapter = true)
            data class InviteAccepted(
                @Json(name = "id")
                override val id: Long = generateId(),
                @Json(name = "jsonrpc")
                override val jsonrpc: String = "2.0",
                @Json(name = "method")
                override val method: String = Web3InboxMethods.Call.Chat.INVITE_ACCEPTED,
                @Json(name = "params")
                override val params: Web3InboxParams.Call.Chat.InviteAcceptedParams,
            ) : Chat

            @JsonClass(generateAdapter = true)
            data class InviteRejected(
                @Json(name = "id")
                override val id: Long = generateId(),
                @Json(name = "jsonrpc")
                override val jsonrpc: String = "2.0",
                @Json(name = "method")
                override val method: String = Web3InboxMethods.Call.Chat.INVITE_REJECTED,
                @Json(name = "params")
                override val params: Web3InboxParams.Call.Chat.InviteRejectedParams,
            ) : Chat

            @JsonClass(generateAdapter = true)
            data class Leave(
                @Json(name = "id")
                override val id: Long = generateId(),
                @Json(name = "jsonrpc")
                override val jsonrpc: String = "2.0",
                @Json(name = "method")
                override val method: String = Web3InboxMethods.Call.Chat.LEAVE,
                @Json(name = "params")
                override val params: Web3InboxParams.Call.Chat.LeaveParams,
            ) : Chat
        }
    }
}