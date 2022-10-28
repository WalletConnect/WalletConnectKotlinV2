package com.walletconnect.chat.common.model

import com.walletconnect.foundation.common.model.PublicKey

internal data class Contact(
    val accountId: AccountId,
    val publicKey: PublicKey,
    val displayName: String
)
