package com.walletconnect.android.verify.data.model

import com.walletconnect.android.internal.common.model.Validation

data class VerifyContext(
    val origin: String,
    val validation: Validation,
    val verifyUrl: String
)