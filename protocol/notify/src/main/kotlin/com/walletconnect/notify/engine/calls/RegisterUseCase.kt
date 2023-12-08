@file:JvmSynthetic

package com.walletconnect.notify.engine.calls

import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.model.ProjectId
import com.walletconnect.android.internal.common.signing.cacao.Cacao
import com.walletconnect.android.internal.common.signing.cacao.CacaoType
import com.walletconnect.android.internal.common.signing.cacao.CacaoVerifier
import com.walletconnect.android.internal.common.signing.cacao.Issuer
import com.walletconnect.android.keyserver.domain.IdentitiesInteractor
import com.walletconnect.foundation.util.jwt.encodeEd25519DidKey
import com.walletconnect.notify.common.Statement
import com.walletconnect.notify.common.model.CacaoPayloadWithIdentityPrivateKey
import com.walletconnect.notify.data.storage.RegisteredAccountsRepository
import com.walletconnect.notify.engine.domain.WatchSubscriptionsUseCase
import kotlinx.coroutines.supervisorScope

internal class RegisterUseCase(
    private val identitiesInteractor: IdentitiesInteractor,
    private val registeredAccountsRepository: RegisteredAccountsRepository,
    private val watchSubscriptionsUseCase: WatchSubscriptionsUseCase,
    private val keyManagementRepository: KeyManagementRepository,
    private val projectId: ProjectId,
) : RegisterUseCaseInterface {

    override suspend fun register(
        cacaoPayloadWithIdentityPrivateKey: CacaoPayloadWithIdentityPrivateKey,
        signature: Cacao.Signature,
        onSuccess: (String) -> Unit,
        onFailure: (Throwable) -> Unit,
    ) = supervisorScope {
        val (cacaoPayload, identityPrivateKey) = cacaoPayloadWithIdentityPrivateKey
        val accountId = AccountId(Issuer(cacaoPayload.iss).address)
        val identityPublicKey = keyManagementRepository.deriveAndStoreEd25519KeyPair(identityPrivateKey)

        //todo ogarnąć walidację
        if (encodeEd25519DidKey(identityPublicKey.keyAsBytes) != cacaoPayload.aud) return@supervisorScope onFailure(IllegalArgumentException("Invalid aud"))
        runCatching { CacaoVerifier(projectId).verify(Cacao(CacaoType.EIP4361.toHeader(), cacaoPayload, signature)) }.getOrElse { error -> return@supervisorScope onFailure(error) }


        val allApps = Statement.toBoolean(cacaoPayload.statement)
        identitiesInteractor.registerIdentity(identityPublicKey, cacaoPayload, signature).fold(
            onFailure = { error -> onFailure(error) },
            onSuccess = {
                runCatching { registeredAccountsRepository.insertOrIgnoreAccount(accountId, identityPublicKey, !allApps, if (!allApps) cacaoPayload.domain else null) }.fold(
                    onFailure = { error -> onFailure(error) },
                    onSuccess = { watchSubscriptionsUseCase(accountId, onSuccess = { onSuccess(identityPublicKey.keyAsHex) }, onFailure = { error -> onFailure(error) }) }
                )
            }
        )

    }
}

internal interface RegisterUseCaseInterface {
    suspend fun register(
        cacaoPayloadWithIdentityPrivateKey: CacaoPayloadWithIdentityPrivateKey,
        signature: Cacao.Signature,
        onSuccess: (String) -> Unit,
        onFailure: (Throwable) -> Unit,
    )
}