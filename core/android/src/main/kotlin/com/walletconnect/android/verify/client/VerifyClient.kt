package com.walletconnect.android.verify.client

import com.walletconnect.android.internal.common.di.verifyModule
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.android.verify.data.VerifyService
import com.walletconnect.android.verify.data.model.AttestationResult
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import org.koin.core.KoinApplication

internal class VerifyClient(private val koinApp: KoinApplication = wcKoinApp) : VerifyInterface {
    private val verifyService get() = koinApp.koin.get<VerifyService>()

    override fun initialize() {
        koinApp.modules(verifyModule())
    }

    override fun register(attestationId: String, onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun resolve(attestationId: String, onSuccess: (AttestationResult) -> Unit, onError: (Throwable) -> Unit) {
        scope.launch {
            supervisorScope {
                try {
                    val response = verifyService.resolveAttestation(attestationId)
                    if (response.isSuccessful && response.body() != null) {
                        val origin = response.body()!!.origin
                        val isScam = response.body()!!.isScam
                        onSuccess(AttestationResult(origin, isScam))
                    } else {
                        onError(IllegalArgumentException(response.errorBody()?.string()))
                    }
                } catch (e: Exception) {
                    onError(e)
                }
            }
        }
    }
}