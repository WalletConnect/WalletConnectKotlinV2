package com.walletconnect.sync.common.model

import com.walletconnect.android.internal.common.model.AccountId

internal data class Account(
    val accountId: AccountId,
    val entropy: Entropy,
)

