@file:JvmSynthetic

package com.walletconnect.notify.engine.calls

import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.signing.cacao.toCAIP122Message
import com.walletconnect.android.keyserver.domain.IdentitiesInteractor
import com.walletconnect.notify.common.Statement
import com.walletconnect.notify.common.model.CacaoPayloadWithIdentityPrivateKey

internal class PrepareRegistrationUseCase(
    private val identitiesInteractor: IdentitiesInteractor,
    private val identityServerUrl: String,
    private val keyManagementRepository: KeyManagementRepository,
) : PrepareRegistrationUseCaseInterface {

    override suspend fun prepareRegistration(account: String, domain: String, onSuccess: (CacaoPayloadWithIdentityPrivateKey, String) -> Unit, onFailure: (Throwable) -> Unit, allApps: Boolean) {
        val identityPublicKey = identitiesInteractor.generateAndStoreIdentityKeyPair()
        val (_, identityPrivateKey) = keyManagementRepository.getKeyPair(identityPublicKey)

        identitiesInteractor.generatePayload(AccountId(account), identityPublicKey, Statement.fromBoolean(allApps).content, domain, listOf(identityServerUrl))
            .also { keyManagementRepository.removeKeys(identityPublicKey.keyAsHex) }
            .fold(
                onFailure = { error -> onFailure(error) },
                onSuccess = { cacaoPayload -> onSuccess(CacaoPayloadWithIdentityPrivateKey(cacaoPayload, identityPrivateKey), cacaoPayload.toCAIP122Message()) }
            )
    }
}

internal interface PrepareRegistrationUseCaseInterface {
    suspend fun prepareRegistration(account: String, domain: String, onSuccess: (CacaoPayloadWithIdentityPrivateKey, String) -> Unit, onFailure: (Throwable) -> Unit, allApps: Boolean = false)
}