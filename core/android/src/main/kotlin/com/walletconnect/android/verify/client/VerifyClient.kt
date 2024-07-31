package com.walletconnect.android.verify.client

import com.walletconnect.android.internal.common.di.verifyModule
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.android.verify.domain.VerifyRepository
import com.walletconnect.android.verify.domain.AttestationResult
import org.koin.core.KoinApplication

internal class VerifyClient(private val koinApp: KoinApplication = wcKoinApp, ) : VerifyInterface {
    private val verifyRepository by lazy { koinApp.koin.get<VerifyRepository>() }
    override fun initialize() {
        koinApp.modules(verifyModule())

        verifyRepository.getVerifyPublicKey()
    }

    override fun resolve(attestationId: String, onSuccess: (AttestationResult) -> Unit, onError: (Throwable) -> Unit) {
        verifyRepository.resolve(attestationId, onSuccess, onError)
    }

    override fun register(attestationId: String, onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        TODO("Not yet implemented")
    }
}