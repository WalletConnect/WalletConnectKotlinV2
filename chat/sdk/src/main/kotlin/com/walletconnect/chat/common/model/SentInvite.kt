@file:JvmSynthetic

package com.walletconnect.chat.common.model

internal data class SentInvite(
    val id: Long,
    val inviterAccount: AccountId,
    val inviteeAccount: AccountId,
    val message: InviteMessage,
    val status: InviteStatus,
)