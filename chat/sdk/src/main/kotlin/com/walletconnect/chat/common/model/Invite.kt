@file:JvmSynthetic

package com.walletconnect.chat.common.model

internal data class Invite(
    val inviterAccount: AccountId,
    val inviteeAccount: AccountId,
    val message: InviteMessage,
    val inviteePublicKey: String,
)