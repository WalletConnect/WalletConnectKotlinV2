package com.walletconnect.android.verify.domain

import com.walletconnect.android.internal.common.crypto.sha256
import com.walletconnect.android.internal.common.model.Validation
import com.walletconnect.android.internal.common.model.WCRequest
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.storage.verify.VerifyContextStorageRepository
import com.walletconnect.android.verify.client.VerifyInterface
import com.walletconnect.android.verify.model.VerifyContext
import com.walletconnect.utils.Empty
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

class ResolveAttestationIdUseCase(private val verifyInterface: VerifyInterface, private val repository: VerifyContextStorageRepository, private val verifyUrl: String) {

    operator fun invoke(request: WCRequest, metadataUrl: String, linkMode: Boolean? = false, appLink: String? = null, onResolve: (VerifyContext) -> Unit) {
        when {
            linkMode == true && !appLink.isNullOrEmpty() -> resolveLinkMode(request, metadataUrl, appLink, onResolve)
            !request.attestation.isNullOrEmpty() -> resolveVerifyV2(metadataUrl, request, onResolve)
            request.attestation?.isEmpty() == true -> insertContext(VerifyContext(request.id, String.Empty, Validation.UNKNOWN, verifyUrl, null)) { verifyContext -> onResolve(verifyContext) }
            else -> resolveVerifyV1(request, metadataUrl, onResolve)
        }
    }

    private fun resolveLinkMode(
        request: WCRequest,
        metadataUrl: String,
        appLink: String,
        onResolve: (VerifyContext) -> Unit
    ) {
        insertContext(VerifyContext(request.id, metadataUrl, getValidation(metadataUrl, appLink), String.Empty, null)) { verifyContext -> onResolve(verifyContext) }
    }

    private fun resolveVerifyV2(
        metadataUrl: String,
        request: WCRequest,
        onResolve: (VerifyContext) -> Unit
    ) {
        verifyInterface.resolveV2(sha256(request.encryptedMessage.toByteArray()), request.attestation!!, metadataUrl,
            onSuccess = { result ->
                insertContext(VerifyContext(request.id, result.origin, result.validation, verifyUrl, result.isScam)) { verifyContext -> onResolve(verifyContext) }
            },
            onError = {
                insertContext(VerifyContext(request.id, String.Empty, Validation.UNKNOWN, verifyUrl, null)) { verifyContext -> onResolve(verifyContext) }
            }
        )
    }

    private fun resolveVerifyV1(
        request: WCRequest,
        metadataUrl: String,
        onResolve: (VerifyContext) -> Unit
    ) {
        verifyInterface.resolve(sha256(request.message.toByteArray()), metadataUrl,
            onSuccess = { result ->
                insertContext(VerifyContext(request.id, result.origin, result.validation, verifyUrl, result.isScam)) { verifyContext -> onResolve(verifyContext) }
            },
            onError = {
                insertContext(VerifyContext(request.id, String.Empty, Validation.UNKNOWN, verifyUrl, null)) { verifyContext -> onResolve(verifyContext) }
            }
        )
    }

    private fun insertContext(context: VerifyContext, onResolve: (VerifyContext) -> Unit) {
        scope.launch {
            supervisorScope {
                try {
                    repository.insertOrAbort(context)
                    onResolve(context)
                } catch (e: Exception) {
                    onResolve(context)
                }
            }
        }
    }
}