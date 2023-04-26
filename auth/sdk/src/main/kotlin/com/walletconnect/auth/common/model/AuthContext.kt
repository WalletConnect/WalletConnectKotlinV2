package com.walletconnect.auth.common.model

import com.walletconnect.android.internal.common.model.Validation

data class AuthContext(
    val origin: String,
    val validation: Validation,
    val verifyUrl: String
)