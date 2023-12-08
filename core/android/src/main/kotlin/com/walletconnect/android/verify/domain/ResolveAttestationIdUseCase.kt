package com.walletconnect.android.verify.domain

import com.walletconnect.android.internal.common.crypto.sha256
import com.walletconnect.android.internal.common.model.Validation
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.storage.verify.VerifyContextStorageRepository
import com.walletconnect.android.verify.client.VerifyInterface
import com.walletconnect.android.verify.data.model.VerifyContext
import com.walletconnect.utils.Empty
import com.walletconnect.utils.compareDomains
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

class ResolveAttestationIdUseCase(private val verifyInterface: VerifyInterface, private val repository: VerifyContextStorageRepository, private val verifyUrl: String) {

    operator fun invoke(id: Long, jsonPayload: String, metadataUrl: String, onResolve: (VerifyContext) -> Unit) {
        val attestationId = sha256(jsonPayload.toByteArray())

        verifyInterface.resolve(attestationId,
            onSuccess = { attestationResult ->
                val (origin, isScam) = Pair(attestationResult.origin, attestationResult.isScam)
                insertContext(VerifyContext(id, origin, if (compareDomains(metadataUrl, origin)) Validation.VALID else Validation.INVALID, verifyUrl, isScam)) { verifyContext ->
                    onResolve(verifyContext)
                }
            },
            onError = {
                insertContext(VerifyContext(id, String.Empty, Validation.UNKNOWN, verifyUrl, null)) { verifyContext -> onResolve(verifyContext) }
            })
    }

    private fun insertContext(context: VerifyContext, onResolve: (VerifyContext) -> Unit) {
        scope.launch {
            supervisorScope {
                repository.insertOrAbort(context)
                onResolve(context)
            }
        }
    }
}