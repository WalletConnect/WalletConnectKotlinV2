package com.walletconnect.web3.inbox.json_rpc

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.android.internal.common.model.type.JsonRpcClientSync
import com.walletconnect.util.generateId

// TODO: Break up into multiple files
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
                override val method: String = Web3InboxMethods.Request.Chat.REGISTER,
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
                override val method: String = Web3InboxMethods.Request.Chat.RESOLVE,
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
                override val method: String = Web3InboxMethods.Request.Chat.ACCEPT,
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
                override val method: String = Web3InboxMethods.Request.Chat.REJECT,
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
                override val method: String = Web3InboxMethods.Request.Chat.INVITE,
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
                override val method: String = Web3InboxMethods.Request.Chat.GET_RECEIVED_INVITES,
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
                override val method: String = Web3InboxMethods.Request.Chat.GET_SENT_INVITES,
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
                override val method: String = Web3InboxMethods.Request.Chat.GET_THREADS,
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
                override val method: String = Web3InboxMethods.Request.Chat.GET_MESSAGES,
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
                override val method: String = Web3InboxMethods.Request.Chat.MESSAGE,
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
                override val method: String = Web3InboxMethods.Request.Push.GET_ACTIVE_SUBSCRIPTIONS,
                override val params: Web3InboxParams.Request.Empty,
            ) : Push

            @JsonClass(generateAdapter = true)
            data class Approve(
                @Json(name = "id")
                override val id: Long,
                @Json(name = "jsonrpc")
                override val jsonrpc: String = "2.0",
                @Json(name = "method")
                override val method: String = Web3InboxMethods.Request.Push.APPROVE,
                @Json(name = "params")
                override val params: Web3InboxParams.Request.Push.ApproveParams,
            ) : Push

            @JsonClass(generateAdapter = true)
            data class Reject(
                @Json(name = "id")
                override val id: Long,
                @Json(name = "jsonrpc")
                override val jsonrpc: String = "2.0",
                @Json(name = "method")
                override val method: String = Web3InboxMethods.Request.Push.REJECT,
                @Json(name = "params")
                override val params: Web3InboxParams.Request.Push.RejectParams,
            ) : Push

            @JsonClass(generateAdapter = true)
            data class Subscribe(
                @Json(name = "id")
                override val id: Long,
                @Json(name = "jsonrpc")
                override val jsonrpc: String = "2.0",
                @Json(name = "method")
                override val method: String = Web3InboxMethods.Request.Push.SUBSCRIBE,
                @Json(name = "params")
                override val params: Web3InboxParams.Request.Push.SubscribeParams,
            ) : Push

            @JsonClass(generateAdapter = true)
            data class Update(
                @Json(name = "id")
                override val id: Long,
                @Json(name = "jsonrpc")
                override val jsonrpc: String = "2.0",
                @Json(name = "method")
                override val method: String = Web3InboxMethods.Request.Push.UPDATE,
                @Json(name = "params")
                override val params: Web3InboxParams.Request.Push.UpdateParams,
            ) : Push

            @JsonClass(generateAdapter = true)
            data class DeleteSubscription(
                @Json(name = "id")
                override val id: Long,
                @Json(name = "jsonrpc")
                override val jsonrpc: String = "2.0",
                @Json(name = "method")
                override val method: String = Web3InboxMethods.Request.Push.DELETE_SUBSCRIPTION,
                @Json(name = "params")
                override val params: Web3InboxParams.Request.Push.DeleteSubscriptionParams,
            ) : Push

            @JsonClass(generateAdapter = true)
            data class GetMessageHistory(
                @Json(name = "id")
                override val id: Long,
                @Json(name = "jsonrpc")
                override val jsonrpc: String = "2.0",
                @Json(name = "method")
                override val method: String = Web3InboxMethods.Request.Push.GET_MESSAGE_HISTORY,
                @Json(name = "params")
                override val params: Web3InboxParams.Request.Push.GetMessageHistoryParams,
            ) : Push

            @JsonClass(generateAdapter = true)
            data class DeletePushMessage(
                @Json(name = "id")
                override val id: Long,
                @Json(name = "jsonrpc")
                override val jsonrpc: String = "2.0",
                @Json(name = "method")
                override val method: String = Web3InboxMethods.Request.Push.DELETE_PUSH_MESSAGE,
                @Json(name = "params")
                override val params: Web3InboxParams.Request.Push.DeletePushMessageParams,
            ) : Push
        }
    }

    /**
     * [Call] represents communication from Kotlin -> Javascript
     */
    sealed interface Call : Web3InboxRPC {
        override val params: Web3InboxParams.Call

        @JsonClass(generateAdapter = true)
        data class SyncUpdate(
            @Json(name = "id")
            override val id: Long = generateId(),
            @Json(name = "jsonrpc")
            override val jsonrpc: String = "2.0",
            @Json(name = "method")
            override val method: String = Web3InboxMethods.Call.SYNC_UPDATE,
            @Json(name = "params")
            override val params: Web3InboxParams.Call.SyncUpdateParams,
        ) : Push, Chat

        sealed interface Chat : Call {
            override val params: Web3InboxParams.Call

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

        sealed interface Push : Call {
            override val params: Web3InboxParams.Call

            @JsonClass(generateAdapter = true)
            data class Propose(
                @Json(name = "id")
                override val id: Long = generateId(),
                @Json(name = "jsonrpc")
                override val jsonrpc: String = "2.0",
                @Json(name = "method")
                override val method: String = Web3InboxMethods.Call.Push.PROPOSE,
                @Json(name = "params")
                override val params: Web3InboxParams.Call.Push.ProposeParams,
            ) : Push

            @JsonClass(generateAdapter = true)
            data class Message(
                @Json(name = "id")
                override val id: Long = generateId(),
                @Json(name = "jsonrpc")
                override val jsonrpc: String = "2.0",
                @Json(name = "method")
                override val method: String = Web3InboxMethods.Call.Push.MESSAGE,
                @Json(name = "params")
                override val params: Web3InboxParams.Call.Push.MessageParams,
            ) : Push

            @JsonClass(generateAdapter = false)
            data class Subscription(
                @Json(name = "id")
                override val id: Long = generateId(),
                @Json(name = "jsonrpc")
                override val jsonrpc: String = "2.0",
                @Json(name = "method")
                override val method: String = Web3InboxMethods.Call.Push.SUBSCRIPTION,
                @Json(name = "params")
                override val params: Web3InboxParams.Call.Push.Subscription,
            ) : Push

            @JsonClass(generateAdapter = false)
            data class Update(
                @Json(name = "id")
                override val id: Long = generateId(),
                @Json(name = "jsonrpc")
                override val jsonrpc: String = "2.0",
                @Json(name = "method")
                override val method: String = Web3InboxMethods.Call.Push.UPDATE,
                @Json(name = "params")
                override val params: Web3InboxParams.Call.Push.Update,
            ) : Push

            @JsonClass(generateAdapter = true)
            data class Delete(
                @Json(name = "id")
                override val id: Long = generateId(),
                @Json(name = "jsonrpc")
                override val jsonrpc: String = "2.0",
                @Json(name = "method")
                override val method: String = Web3InboxMethods.Call.Push.DELETE,
                @Json(name = "params")
                override val params: Web3InboxParams.Call.Push.DeleteParams,
            ) : Push
        }
    }
}