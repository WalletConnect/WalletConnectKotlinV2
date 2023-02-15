@file:JvmSynthetic

package com.walletconnect.chat.common.model

import com.walletconnect.foundation.common.model.PublicKey

internal sealed interface Invite {
    val id: Long
    val inviterAccount: AccountId
    val inviteeAccount: AccountId
    val message: InviteMessage
    val inviterPublicKey: PublicKey
    val inviteePublicKey: PublicKey
    val status: InviteStatus

    data class Received(
        override val id: Long,
        override val inviterAccount: AccountId,
        override val inviteeAccount: AccountId,
        override val message: InviteMessage,
        override val inviterPublicKey: PublicKey,
        override val inviteePublicKey: PublicKey,
        override val status: InviteStatus,
    ) : Invite

    data class Sent(
        override val id: Long,
        override val inviterAccount: AccountId,
        override val inviteeAccount: AccountId,
        override val message: InviteMessage,
        override val inviterPublicKey: PublicKey,
        override val inviteePublicKey: PublicKey,
        override val status: InviteStatus,
    ) : Invite
}