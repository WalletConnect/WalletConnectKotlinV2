@file:JvmSynthetic

package com.walletconnect.notify.engine.calls

import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.signing.cacao.toCAIP222Message
import com.walletconnect.android.keyserver.domain.IdentitiesInteractor
import com.walletconnect.foundation.util.Logger
import com.walletconnect.notify.common.model.CacaoPayloadWithIdentityPrivateKey
import com.walletconnect.notify.engine.domain.createAuthorizationReCaps

internal class PrepareRegistrationUseCase(
    private val identitiesInteractor: IdentitiesInteractor,
    private val identityServerUrl: String,
    private val keyManagementRepository: KeyManagementRepository,
    private val logger: Logger
) : PrepareRegistrationUseCaseInterface {

    override suspend fun prepareRegistration(
        account: String,
        domain: String,
        onSuccess: (CacaoPayloadWithIdentityPrivateKey, String) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        val identityPublicKey = identitiesInteractor.generateAndStoreIdentityKeyPair()
        val (_, identityPrivateKey) = keyManagementRepository.getKeyPair(identityPublicKey)

        identitiesInteractor.generatePayload(
            AccountId(account),
            identityPublicKey,
            null,
            domain,
            listOf(identityServerUrl, createAuthorizationReCaps())
        )
            .also { keyManagementRepository.removeKeys(identityPublicKey.keyAsHex) }
            .fold(
                onFailure = { error -> onFailure(error) },
                onSuccess = { cacaoPayload ->
                    onSuccess(
                        CacaoPayloadWithIdentityPrivateKey(
                            cacaoPayload,
                            identityPrivateKey
                        ),
                        cacaoPayload.toCAIP222Message()
                    )
                }
            )
    }
}

internal interface PrepareRegistrationUseCaseInterface {
    suspend fun prepareRegistration(
        account: String,
        domain: String,
        onSuccess: (CacaoPayloadWithIdentityPrivateKey, String) -> Unit,
        onFailure: (Throwable) -> Unit,
    )
}