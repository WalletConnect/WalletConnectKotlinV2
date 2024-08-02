package com.walletconnect.android.verify.client

import com.walletconnect.android.internal.common.di.verifyModule
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.android.verify.domain.AttestationResult
import com.walletconnect.android.verify.domain.VerifyRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.KoinApplication

internal class VerifyClient(private val koinApp: KoinApplication = wcKoinApp, ) : VerifyInterface {
    private val verifyRepository by lazy { koinApp.koin.get<VerifyRepository>() }
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    override fun initialize() {
        koinApp.modules(verifyModule())

        scope.launch {
            verifyRepository.getVerifyPublicKey().onFailure { throwable -> println("kobe: Error fetching a key: ${throwable.message}") }
        }
    }

    override fun resolve(attestationId: String, onSuccess: (AttestationResult) -> Unit, onError: (Throwable) -> Unit) {
        verifyRepository.resolve(attestationId, onSuccess, onError)
    }

    override fun resolveV2(attestation: String, onSuccess: (AttestationResult) -> Unit, onError: (Throwable) -> Unit) {
        verifyRepository.resolveV2(attestation, onSuccess, onError)
    }

    override fun register(attestationId: String, onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        TODO("Not yet implemented")
    }
}