package com.walletconnect.android.verify.domain

data class AttestationResult(
    val origin: String,
    val isScam: Boolean?
)