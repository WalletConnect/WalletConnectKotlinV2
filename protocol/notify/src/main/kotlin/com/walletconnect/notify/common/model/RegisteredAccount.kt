package com.walletconnect.notify.common.model

import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.foundation.common.model.PublicKey

data class RegisteredAccount(
    val accountId: AccountId,
    val publicIdentityKey: PublicKey,
    val isLimited: Boolean,
    val appDomain: String?
)