package com.walletconnect.android.verify.domain

import com.walletconnect.android.internal.common.model.Validation

data class VerifyResult(
    val validation: Validation,
    val isScam: Boolean?,
    val origin: String
)