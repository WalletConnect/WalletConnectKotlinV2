@file:JvmSynthetic

package com.walletconnect.chat.common.model

import com.walletconnect.foundation.common.model.PublicKey

internal data class AccountIdWithPublicKey(
    val accountId: AccountId,
    val publicKey: PublicKey,
)