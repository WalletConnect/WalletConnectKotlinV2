@file:JvmSynthetic

package com.walletconnect.chat.common.model

import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic

internal data class Account(
    val accountId: AccountId,
    val publicIdentityKey: PublicKey,
    val publicInviteKey: PublicKey?,
    val inviteTopic: Topic?,
)