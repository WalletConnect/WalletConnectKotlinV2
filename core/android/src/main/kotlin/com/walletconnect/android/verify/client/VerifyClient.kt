package com.walletconnect.android.verify.client

import com.walletconnect.android.internal.common.di.verifyModule
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.android.verify.domain.VerifyRepository
import com.walletconnect.android.verify.domain.VerifyResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.KoinApplication

internal class VerifyClient(
    private val koinApp: KoinApplication = wcKoinApp,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) : VerifyInterface {
    private val verifyRepository by lazy { koinApp.koin.get<VerifyRepository>() }

    override fun initialize() {
        koinApp.modules(verifyModule())

        scope.launch {
            verifyRepository.getVerifyPublicKey().onFailure { throwable -> println("Error fetching a key: ${throwable.message}") }
        }
    }

    override fun resolve(attestationId: String, metadataUrl: String, onSuccess: (VerifyResult) -> Unit, onError: (Throwable) -> Unit) {
        verifyRepository.resolve(attestationId, metadataUrl, onSuccess, onError)
    }

    override fun resolveV2(attestation: String, metadataUrl: String, onSuccess: (VerifyResult) -> Unit, onError: (Throwable) -> Unit) {
        verifyRepository.resolveV2(attestation, metadataUrl, onSuccess, onError)
    }

    override fun register(attestationId: String, onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        TODO("Not yet implemented")
    }
}