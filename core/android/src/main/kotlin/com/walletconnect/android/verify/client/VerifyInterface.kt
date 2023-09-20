package com.walletconnect.android.verify.client

import com.walletconnect.android.verify.data.model.AttestationResult

interface VerifyInterface {
    fun initialize(verifyUrl: String?)
    fun register(attestationId: String, onSuccess: () -> Unit, onError: (Throwable) -> Unit)
    fun resolve(attestationId: String, onSuccess: (AttestationResult) -> Unit, onError: (Throwable) -> Unit)
}