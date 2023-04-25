package com.walletconnect.android.verify

import com.walletconnect.android.internal.common.di.verifyModule
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.android.verify.data.VerifyService
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import org.koin.core.qualifier.named

internal object VerifyClient : VerifyInterface {
    private val verifyService get() = wcKoinApp.koin.get<VerifyService>(named("VerifyService"))
    private const val SUCCESS_STATUS = "SUCCESS"

    override fun initialize(verifyUrl: String?) {
        wcKoinApp.modules(verifyModule(verifyUrl))
    }

    override fun register(attestationId: String, onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun resolve(attestationId: String, onSuccess: (String) -> Unit, onError: (Throwable) -> Unit) {
        scope.launch {
            supervisorScope {
                try {
                    val response = verifyService.resolveAttestation(attestationId)
                    if (response.isSuccessful && response.body() != null) {
                        onSuccess(response.body()!!.origin)
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