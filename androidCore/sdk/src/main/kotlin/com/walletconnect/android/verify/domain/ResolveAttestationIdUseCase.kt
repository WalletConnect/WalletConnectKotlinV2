package com.walletconnect.android.verify.domain

import com.walletconnect.android.internal.common.crypto.sha256
import com.walletconnect.android.internal.common.model.Validation
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.storage.VerifyContextStorageRepository
import com.walletconnect.android.verify.client.VerifyInterface
import com.walletconnect.android.verify.data.model.VerifyContext
import com.walletconnect.utils.Empty
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

class ResolveAttestationIdUseCase(private val verifyInterface: VerifyInterface, private val repository: VerifyContextStorageRepository, private val verifyUrl: String) {

    operator fun invoke(id: Long, jsonPayload: String, metadataUrl: String, onResolve: (VerifyContext) -> Unit) {
        val attestationId = sha256(jsonPayload.toByteArray())

        verifyInterface.resolve(attestationId,
            onSuccess = { origin ->
                insertContext(VerifyContext(id, origin, if (metadataUrl == origin) Validation.VALID else Validation.INVALID, verifyUrl)) { verifyContext -> onResolve(verifyContext) }
            },
            onError = {
                insertContext(VerifyContext(id, String.Empty, Validation.UNKNOWN, verifyUrl)) { verifyContext -> onResolve(verifyContext) }
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