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

    operator fun invoke(jsonPayload: String, metadataUrl: String, onResolve: (VerifyContext) -> Unit) {
        println("kobe; URL: $metadataUrl; PAYLOAD: $jsonPayload")
        val attestationId = sha256(jsonPayload.toByteArray())

        verifyInterface.resolve(attestationId,
            onSuccess = { origin ->
                println("kobe; Success: $origin")
                val validation = if (metadataUrl == origin) Validation.VALID else Validation.INVALID
                onResolve(VerifyContext(origin, validation, verifyUrl))
            },
            onError = { error ->
                println("kobe; Error: $error")
                onResolve(VerifyContext("", Validation.UNKNOWN, verifyUrl))
            })
    }
}