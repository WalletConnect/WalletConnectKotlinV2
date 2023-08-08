package com.walletconnect.web3.inbox.json_rpc

import com.squareup.moshi.JsonClass
import com.walletconnect.android.internal.common.model.type.ClientParams

// TODO: Break up into multiple files
internal sealed interface Web3InboxParams : ClientParams {

    sealed interface Request : Web3InboxParams {

        // note: If this is an object it breaks serialization
        class Empty : Request {
            override fun equals(other: Any?): Boolean {
                return this === other
            }

            override fun hashCode(): Int {
                return System.identityHashCode(this)
            }
        }

        sealed interface Chat : Request {
            @JsonClass(generateAdapter = true)
            data class RegisterParams(
                val account: String,
                val private: Boolean?,
            ) : Chat

            @JsonClass(generateAdapter = true)
            data class ResolveParams(
                val account: String,
            ) : Chat

            @JsonClass(generateAdapter = true)
            data class AcceptParams(
                val id: Long,
            ) : Chat

            @JsonClass(generateAdapter = true)
            data class RejectParams(
                val id: Long,
            ) : Chat

            @JsonClass(generateAdapter = true)
            data class InviteParams(
                val inviteeAccount: String,
                val inviterAccount: String,
                val inviteePublicKey: String,
                val message: String,
            ) : Chat

            @JsonClass(generateAdapter = true)
            data class GetReceivedInvitesParams(
                val account: String,
            ) : Chat

            @JsonClass(generateAdapter = true)
            data class GetSentInvitesParams(
                val account: String,
            ) : Chat

            @JsonClass(generateAdapter = true)
            data class GetThreadsParams(
                val account: String,
            ) : Chat

            @JsonClass(generateAdapter = true)
            data class GetMessagesParams(
                val topic: String,
            ) : Chat

            @JsonClass(generateAdapter = true)
            data class MessageParams(
                val topic: String,
                val authorAccount: String,
                val message: String,
                val timestamp: Long,
            ) : Chat
        }

        sealed interface Notify : Request {

            @JsonClass(generateAdapter = true)
            data class SubscribeParams(
                val metadata: AppMetaDataParams,
                val account: String,
            ) : Notify

            @JsonClass(generateAdapter = true)
            data class UpdateParams(
                val topic: String,
                val scope: List<String>,
            ) : Notify

            @JsonClass(generateAdapter = true)
            data class DeleteSubscriptionParams(
                val topic: String,
            ) : Notify

            @JsonClass(generateAdapter = true)
            data class GetMessageHistoryParams(
                val topic: String,
            ) : Notify

            @JsonClass(generateAdapter = true)
            data class DeleteNotifyMessageParams(
                val id: Long,
            ) : Notify

            @JsonClass(generateAdapter = true)
            data class EnableSyncParams(
                val account: String,
                val private: Boolean?,
            ) : Notify
        }
    }

    sealed interface Response : Web3InboxParams {

        sealed interface Chat : Response {
            @JsonClass(generateAdapter = true)
            data class GetReceivedInvitesResult(
                val id: Long,
                val inviterAccount: String,
                val inviteeAccount: String,
                val message: String,
                val inviterPublicKey: String,
                val status: String,
            ) : Chat

            @JsonClass(generateAdapter = true)
            data class GetSentInvitesResult(
                val id: Long,
                val inviterAccount: String,
                val inviteeAccount: String,
                val message: String,
                val inviterPublicKey: String,
                val status: String,
            ) : Chat

            @JsonClass(generateAdapter = true)
            data class GetThreadsResult(
                val topic: String,
                val selfAccount: String,
                val peerAccount: String,
            ) : Chat

            @JsonClass(generateAdapter = true)
            data class GetMessagesResult(
                val topic: String,
                val message: String,
                val authorAccount: String,
                val timestamp: Long,
                val media: MediaResult?,
            ) : Chat {

                @JsonClass(generateAdapter = true)
                data class MediaResult(
                    val type: String,
                    val data: String,
                )
            }
        }

        sealed interface Notify : Response {
            @JsonClass(generateAdapter = true)
            data class GetActiveSubscriptionsResult(
                val topic: String,
                val account: String,
                val relay: RelayParams,
                val metadata: AppMetaDataParams,
            ) : Response
        }
    }

    sealed interface Call : Web3InboxParams {

        // note: If this is an object it breaks serialization
        class Empty : Call {
            override fun equals(other: Any?): Boolean {
                return this === other
            }

            override fun hashCode(): Int {
                return System.identityHashCode(this)
            }
        }

        sealed interface Chat : Call {
            @JsonClass(generateAdapter = true)
            data class InviteParams(
                val id: Long,
                val inviterAccount: String,
                val inviteeAccount: String,
                val message: String,
                val inviterPublicKey: String,
                val status: String,
            ) : Chat

            @JsonClass(generateAdapter = true)
            data class MessageParams(
                val topic: String,
                val message: String,
                val authorAccount: String,
                val timestamp: Long,
                val media: MediaParams?,
            ) : Chat {

                @JsonClass(generateAdapter = true)
                data class MediaParams(
                    val type: String,
                    val data: String,
                )
            }

            @JsonClass(generateAdapter = true)
            data class InviteAcceptedParams(
                val topic: String,
                val invite: InviteParams,
            ) : Chat

            @JsonClass(generateAdapter = true)
            data class InviteRejectedParams(
                val invite: InviteParams,
            ) : Chat

            @JsonClass(generateAdapter = true)
            data class LeaveParams(
                val topic: String,
            ) : Chat
        }

        sealed interface Notify : Call {

            @JsonClass(generateAdapter = true)
            data class MessageParams(
                val message: MessageRecord,
            ) : Notify {
                data class MessageRecord(
                    val id: String,
                    val topic: String,
                    val publishedAt: Long,
                    val message: Message,
                )

                data class Message(
                    val title: String,
                    val body: String,
                    val icon: String?,
                    val url: String?,
                )
            }

            sealed interface Subscription : Notify {
                @JsonClass(generateAdapter = true)
                data class ResultParams(val subscription: SubscriptionParams) : Subscription

                @JsonClass(generateAdapter = true)
                data class ErrorParams(val id: Long, val reason: String) : Subscription
            }

            sealed interface Update : Notify {
                @JsonClass(generateAdapter = true)
                data class ResultParams(val subscription: SubscriptionParams) : Update

                @JsonClass(generateAdapter = true)
                data class ErrorParams(val id: Long, val reason: String) : Update
            }

            @JsonClass(generateAdapter = true)
            data class DeleteParams(
                val topic: String,
            ) : Notify
        }
    }

    @JsonClass(generateAdapter = true)
    data class SubscriptionParams(
        val topic: String,
        val account: String,
        val relay: RelayParams,
        val metadata: AppMetaDataParams,
        val scope: Map<String, ScopeSettingParams>,
        val expiry: Long,
    ) : Web3InboxParams

    @JsonClass(generateAdapter = true)
    data class RelayParams(val protocol: String, val data: String?) : Web3InboxParams

    @JsonClass(generateAdapter = true)
    data class AppMetaDataParams(val name: String, val description: String, val url: String, val icons: List<String>, val redirect: String?, val verifyUrl: String? = null) : Web3InboxParams

    @JsonClass(generateAdapter = true)
    data class ScopeSettingParams(val description: String, val enabled: Boolean) : Web3InboxParams
}