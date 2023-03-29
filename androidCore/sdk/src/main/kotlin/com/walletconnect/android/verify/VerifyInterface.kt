package com.walletconnect.android.verify

interface VerifyInterface {
    fun initialize(verifyUrl: String?)
    fun register(attestationId: String)
    fun resolve(attestationId: String, onSuccess: (String) -> Unit)
}