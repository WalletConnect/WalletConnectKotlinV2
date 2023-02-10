@file:JvmSynthetic

package com.walletconnect.chat.common.model

internal data class ReceivedInvite(
    val id: Long,
    val inviterAccount: AccountId,
    val inviteeAccount: AccountId,
    val message: InviteMessage,
    val inviterPublicKey: String,
    val inviteePublicKey: String,
)