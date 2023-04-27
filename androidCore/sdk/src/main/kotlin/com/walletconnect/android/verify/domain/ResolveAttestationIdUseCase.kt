package com.walletconnect.android.verify.domain

import com.walletconnect.android.internal.common.crypto.sha256
import com.walletconnect.android.internal.common.di.AndroidCommonDITags
import com.walletconnect.android.internal.common.model.Validation
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.android.verify.client.VerifyInterface
import com.walletconnect.android.verify.data.model.VerifyContext
import org.koin.core.qualifier.named

class ResolveAttestationIdUseCase(private val verifyInterface: VerifyInterface) {
    val verifyUrl: String get() = wcKoinApp.koin.get(named(AndroidCommonDITags.VERIFY_URL))

    operator fun invoke(id: Long, jsonPayload: String, metadataUrl: String, onResolve: (VerifyContext) -> Unit) {
        val attestationId = sha256(jsonPayload.toByteArray())

        verifyInterface.resolve(attestationId,
            onSuccess = { origin -> onResolve(VerifyContext(id, origin, if (metadataUrl == origin) Validation.VALID else Validation.INVALID, verifyUrl)) },
            onError = { onResolve(VerifyContext(id, "", Validation.UNKNOWN, verifyUrl)) })
    }
}