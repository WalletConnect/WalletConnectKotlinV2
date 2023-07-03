package com.walletconnect.android.verify.client

interface VerifyInterface {
    fun initialize(verifyUrl: String?)
    fun register(attestationId: String, onSuccess: () -> Unit, onError: (Throwable) -> Unit)
    fun resolve(attestationId: String, onSuccess: (String) -> Unit, onError: (Throwable) -> Unit)
}