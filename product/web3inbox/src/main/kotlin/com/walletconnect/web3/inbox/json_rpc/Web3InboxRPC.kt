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
                @Json(name = "topic")
                override var topic: String? = null,
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
                @Json(name = "topic")
                override var topic: String? = null,
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
                @Json(name = "topic")
                override var topic: String? = null,
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
                @Json(name = "topic")
                override var topic: String? = null,
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
                @Json(name = "topic")
                override var topic: String? = null,
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
                @Json(name = "topic")
                override var topic: String? = null,
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
                @Json(name = "topic")
                override var topic: String? = null,
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
                @Json(name = "topic")
                override var topic: String? = null,
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
                @Json(name = "topic")
                override var topic: String? = null,
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
                @Json(name = "topic")
                override var topic: String? = null,
            ) : Chat
        }

        sealed interface Notify : Request {
            @JsonClass(generateAdapter = true)
            data class GetActiveSubscriptions(
                @Json(name = "id")
                override val id: Long,
                @Json(name = "jsonrpc")
                override val jsonrpc: String = "2.0",
                @Json(name = "method")
                override val method: String = Web3InboxMethods.Request.Notify.GET_ACTIVE_SUBSCRIPTIONS,
                override val params: Web3InboxParams.Request.Empty,
                @Json(name = "topic")
                override var topic: String? = null,
            ) : Notify

            @JsonClass(generateAdapter = true)
            data class Subscribe(
                @Json(name = "id")
                override val id: Long,
                @Json(name = "jsonrpc")
                override val jsonrpc: String = "2.0",
                @Json(name = "method")
                override val method: String = Web3InboxMethods.Request.Notify.SUBSCRIBE,
                @Json(name = "params")
                override val params: Web3InboxParams.Request.Notify.SubscribeParams,
                @Json(name = "topic")
                override var topic: String? = null,
            ) : Notify

            @JsonClass(generateAdapter = true)
            data class Update(
                @Json(name = "id")
                override val id: Long,
                @Json(name = "jsonrpc")
                override val jsonrpc: String = "2.0",
                @Json(name = "method")
                override val method: String = Web3InboxMethods.Request.Notify.UPDATE,
                @Json(name = "params")
                override val params: Web3InboxParams.Request.Notify.UpdateParams,
                @Json(name = "topic")
                override var topic: String? = null,
            ) : Notify

            @JsonClass(generateAdapter = true)
            data class DeleteSubscription(
                @Json(name = "id")
                override val id: Long,
                @Json(name = "jsonrpc")
                override val jsonrpc: String = "2.0",
                @Json(name = "method")
                override val method: String = Web3InboxMethods.Request.Notify.DELETE_SUBSCRIPTION,
                @Json(name = "params")
                override val params: Web3InboxParams.Request.Notify.DeleteSubscriptionParams,
                @Json(name = "topic")
                override var topic: String? = null,
            ) : Notify

            @JsonClass(generateAdapter = true)
            data class GetMessageHistory(
                @Json(name = "id")
                override val id: Long,
                @Json(name = "jsonrpc")
                override val jsonrpc: String = "2.0",
                @Json(name = "method")
                override val method: String = Web3InboxMethods.Request.Notify.GET_MESSAGE_HISTORY,
                @Json(name = "params")
                override val params: Web3InboxParams.Request.Notify.GetMessageHistoryParams,
                @Json(name = "topic")
                override var topic: String? = null,
            ) : Notify

            @JsonClass(generateAdapter = true)
            data class DeleteNotifyMessage(
                @Json(name = "id")
                override val id: Long,
                @Json(name = "jsonrpc")
                override val jsonrpc: String = "2.0",
                @Json(name = "method")
                override val method: String = Web3InboxMethods.Request.Notify.DELETE_NOTIFY_MESSAGE,
                @Json(name = "params")
                override val params: Web3InboxParams.Request.Notify.DeleteNotifyMessageParams,
                @Json(name = "topic")
                override var topic: String? = null,
            ) : Notify

            @JsonClass(generateAdapter = true)
            data class Register(
                @Json(name = "id")
                override val id: Long,
                @Json(name = "jsonrpc")
                override val jsonrpc: String = "2.0",
                @Json(name = "method")
                override val method: String = Web3InboxMethods.Request.Notify.REGISTER,
                @Json(name = "params")
                override val params: Web3InboxParams.Request.Notify.RegisterParams,
                @Json(name = "topic")
                override var topic: String? = null,
            ) : Notify
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
            override val params: Web3InboxParams.Call.Empty =  Web3InboxParams.Call.Empty(),
            @Json(name = "topic")
            override var topic: String? = null,
        ) : Notify, Chat

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
                @Json(name = "topic")
                override var topic: String? = null,
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
                @Json(name = "topic")
                override var topic: String? = null,
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
                @Json(name = "topic")
                override var topic: String? = null,
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
                @Json(name = "topic")
                override var topic: String? = null,
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
                @Json(name = "topic")
                override var topic: String? = null,
            ) : Chat
        }

        sealed interface Notify : Call {
            override val params: Web3InboxParams.Call

            @JsonClass(generateAdapter = true)
            data class Message(
                @Json(name = "id")
                override val id: Long = generateId(),
                @Json(name = "jsonrpc")
                override val jsonrpc: String = "2.0",
                @Json(name = "method")
                override val method: String = Web3InboxMethods.Call.Notify.MESSAGE,
                @Json(name = "params")
                override val params: Web3InboxParams.Call.Notify.MessageParams,
                @Json(name = "topic")
                override var topic: String? = null,
            ) : Notify

            @JsonClass(generateAdapter = false)
            data class Subscription(
                @Json(name = "id")
                override val id: Long = generateId(),
                @Json(name = "jsonrpc")
                override val jsonrpc: String = "2.0",
                @Json(name = "method")
                override val method: String = Web3InboxMethods.Call.Notify.SUBSCRIPTION,
                @Json(name = "params")
                override val params: Web3InboxParams.Call.Notify.Subscription,
                @Json(name = "topic")
                override var topic: String? = null,
            ) : Notify

            @JsonClass(generateAdapter = false)
            data class Update(
                @Json(name = "id")
                override val id: Long = generateId(),
                @Json(name = "jsonrpc")
                override val jsonrpc: String = "2.0",
                @Json(name = "method")
                override val method: String = Web3InboxMethods.Call.Notify.UPDATE,
                @Json(name = "params")
                override val params: Web3InboxParams.Call.Notify.Update,
                @Json(name = "topic")
                override var topic: String? = null,
            ) : Notify

            @JsonClass(generateAdapter = true)
            data class Delete(
                @Json(name = "id")
                override val id: Long = generateId(),
                @Json(name = "jsonrpc")
                override val jsonrpc: String = "2.0",
                @Json(name = "method")
                override val method: String = Web3InboxMethods.Call.Notify.DELETE,
                @Json(name = "params")
                override val params: Web3InboxParams.Call.Notify.DeleteParams,
                @Json(name = "topic")
                override var topic: String? = null,
            ) : Notify
        }
    }
}