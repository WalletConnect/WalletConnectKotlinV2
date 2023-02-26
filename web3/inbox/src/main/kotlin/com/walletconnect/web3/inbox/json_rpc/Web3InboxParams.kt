package com.walletconnect.web3.inbox.json_rpc

import com.squareup.moshi.JsonClass
import com.walletconnect.android.internal.common.model.type.ClientParams

internal sealed interface Web3InboxParams : ClientParams {

    sealed interface Request : Web3InboxParams {
        @JsonClass(generateAdapter = true)
        data class RegisterParams(
            val account: String,
            val private: Boolean?,
        ) : Request

        @JsonClass(generateAdapter = true)
        data class ResolveParams(
            val account: String,
        ) : Request

        @JsonClass(generateAdapter = true)
        data class AcceptParams(
            val id: Long,
        ) : Request

        @JsonClass(generateAdapter = true)
        data class RejectParams(
            val id: Long,
        ) : Request

        @JsonClass(generateAdapter = true)
        data class InviteParams(
            val inviteeAccount: String,
            val inviterAccount: String,
            val inviteePublicKey: String,
            val message: String,
        ) : Request

        @JsonClass(generateAdapter = true)
        data class GetReceivedInvitesParams(
            val account: String,
        ) : Request

        @JsonClass(generateAdapter = true)
        data class GetSentInvitesParams(
            val account: String,
        ) : Request

        @JsonClass(generateAdapter = true)
        data class GetThreadsParams(
            val account: String,
        ) : Request

        @JsonClass(generateAdapter = true)
        data class GetMessagesParams(
            val topic: String,
        ) : Request

        @JsonClass(generateAdapter = true)
        data class MessageParams(
            val topic: String,
            val authorAccount: String,
            val message: String,
            val timestamp: Long,
        ) : Request
    }

    sealed interface Response : Web3InboxParams {
        @JsonClass(generateAdapter = true)
        data class GetReceivedInvitesResult(
            val id: Long,
            val inviterAccount: String,
            val inviteeAccount: String,
            val message: String,
            val inviterPublicKey: String,
            val inviteePublicKey: String,
            val status: String,
        ) : Response

        @JsonClass(generateAdapter = true)
        data class GetSentInvitesResult(
            val id: Long,
            val inviterAccount: String,
            val inviteeAccount: String,
            val message: String,
            val inviterPublicKey: String,
            val inviteePublicKey: String,
            val status: String,
        ) : Response

        @JsonClass(generateAdapter = true)
        data class GetThreadsResult(
            val topic: String,
            val selfAccount: String,
            val peerAccount: String,
        ) : Response

        @JsonClass(generateAdapter = true)
        data class GetMessagesResult(
            val topic: String,
            val message: String,
            val authorAccount: String,
            val timestamp: Long,
            val media: MediaResult?,
        ) : Response {

            @JsonClass(generateAdapter = true)
            data class MediaResult(
                val type: String,
                val data: String,
            )
        }
    }

    sealed interface Call : Web3InboxParams {
        sealed interface Chat : Call {
            @JsonClass(generateAdapter = true)
            data class InviteParams(
                val id: Long,
                val inviterAccount: String,
                val inviteeAccount: String,
                val message: String,
                val inviterPublicKey: String,
                val inviteePublicKey: String,
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
                ) : Chat {
                @JsonClass(generateAdapter = true)
                data class InviteParams(
                    val id: Long,
                    val inviterAccount: String,
                    val inviteeAccount: String,
                    val message: String,
                    val inviterPublicKey: String,
                    val inviteePublicKey: String,
                    val status: String,
                )
            }

            @JsonClass(generateAdapter = true)
            data class InviteRejectedParams(
                val invite: InviteParams,
            ) : Chat {
                @JsonClass(generateAdapter = true)
                data class InviteParams(
                    val id: Long,
                    val inviterAccount: String,
                    val inviteeAccount: String,
                    val message: String,
                    val inviterPublicKey: String,
                    val inviteePublicKey: String,
                    val status: String,
                )
            }

            @JsonClass(generateAdapter = true)
            data class LeaveParams(
                val topic: String,
            ) : Chat
        }
    }
}
