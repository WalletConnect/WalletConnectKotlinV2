package com.walletconnect.android.verify.domain

import com.walletconnect.android.internal.common.crypto.sha256
import com.walletconnect.android.internal.common.model.Validation
import com.walletconnect.android.internal.common.model.WCRequest
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.storage.verify.VerifyContextStorageRepository
import com.walletconnect.android.verify.client.VerifyInterface
import com.walletconnect.android.verify.model.VerifyContext
import com.walletconnect.utils.Empty
import com.walletconnect.utils.compareDomains
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

class ResolveAttestationIdUseCase(private val verifyInterface: VerifyInterface, private val repository: VerifyContextStorageRepository, private val verifyUrl: String) {

    operator fun invoke(request: WCRequest, metadataUrl: String, linkMode: Boolean? = false, appLink: String? = null, onResolve: (VerifyContext) -> Unit) {
        if (linkMode == true && !appLink.isNullOrEmpty()) {
            insertContext(VerifyContext(request.id, metadataUrl, getValidation(metadataUrl, appLink), String.Empty, null)) { verifyContext ->
                onResolve(verifyContext)
            }
        } else {
            when {
                !request.attestation.isNullOrEmpty() -> {
                    println("kobe: Verify v2")
                    verifyInterface.resolveV2(request.attestation,
                        onSuccess = { attestationResult ->
//                            val (origin, isScam) = Pair(attestationResult.origin, attestationResult.isScam)
//                            insertContext(VerifyContext(request.id, origin, getValidation(metadataUrl, origin), verifyUrl, isScam)) { verifyContext ->
//                                onResolve(verifyContext)
//                            }
                        },
                        onError = {
                            insertContext(VerifyContext(request.id, String.Empty, Validation.UNKNOWN, verifyUrl, null)) { verifyContext -> onResolve(verifyContext) }
                        }
                    )
                }

                else -> {
                    println("kobe: Verify v1 - backward compatibility")
                    val attestationId = sha256(request.message.toByteArray())
                    verifyInterface.resolve(attestationId,
                        onSuccess = { attestationResult ->
                            val (origin, isScam) = Pair(attestationResult.origin, attestationResult.isScam)
                            insertContext(VerifyContext(request.id, origin, getValidation(metadataUrl, origin), verifyUrl, isScam)) { verifyContext ->
                                onResolve(verifyContext)
                            }
                        },
                        onError = {
                            insertContext(VerifyContext(request.id, String.Empty, Validation.UNKNOWN, verifyUrl, null)) { verifyContext -> onResolve(verifyContext) }
                        }
                    )
                }
            }
        }
    }

    private fun getValidation(metadataUrl: String, appLink: String) = if (compareDomains(metadataUrl, appLink)) Validation.VALID else Validation.INVALID

    private fun insertContext(context: VerifyContext, onResolve: (VerifyContext) -> Unit) {
        scope.launch {
            supervisorScope {
                repository.insertOrAbort(context)
                onResolve(context)
            }
        }
    }
}