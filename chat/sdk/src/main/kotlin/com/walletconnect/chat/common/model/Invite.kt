@file:JvmSynthetic

package com.walletconnect.chat.common.model

internal sealed interface Invite {
    val id: Long
    val inviterAccount: AccountId
    val inviteeAccount: AccountId
    val message: InviteMessage
    val inviterPublicKey: String
    val inviteePublicKey: String
    val status: InviteStatus

    data class Received(
        override val id: Long,
        override val inviterAccount: AccountId,
        override val inviteeAccount: AccountId,
        override val message: InviteMessage,
        override val inviterPublicKey: String,
        override val inviteePublicKey: String,
        override val status: InviteStatus,
    ) : Invite

    data class Sent(
        override val id: Long,
        override val inviterAccount: AccountId,
        override val inviteeAccount: AccountId,
        override val message: InviteMessage,
        override val inviterPublicKey: String,
        override val inviteePublicKey: String,
        override val status: InviteStatus,
    ) : Invite
}