package com.walletconnect.android.verify.client

import com.walletconnect.android.verify.domain.VerifyResult

interface VerifyInterface {
    fun initialize()
    fun register(attestationId: String, onSuccess: () -> Unit, onError: (Throwable) -> Unit)
    fun resolve(attestationId: String, metadataUrl: String, onSuccess: (VerifyResult) -> Unit, onError: (Throwable) -> Unit)
    fun resolveV2(attestation: String, metadataUrl: String, onSuccess: (VerifyResult) -> Unit, onError: (Throwable) -> Unit)
}