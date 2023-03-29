@file:JvmSynthetic

package com.walletconnect.chat.common.model

import com.walletconnect.android.internal.common.model.AccountId

internal data class SendInvite(val inviterAccount: AccountId, val inviteeAccount: AccountId, val message: InviteMessage, val inviteePublicKey: String)