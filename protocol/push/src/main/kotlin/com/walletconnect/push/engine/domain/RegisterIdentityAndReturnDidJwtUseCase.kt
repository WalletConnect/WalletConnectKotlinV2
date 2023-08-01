package com.walletconnect.push.engine.domain

import com.walletconnect.android.internal.common.jwt.did.EncodeDidJwtPayloadUseCase
import com.walletconnect.android.internal.common.jwt.did.encodeDidJwt
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.model.DidJwt
import com.walletconnect.android.internal.common.signing.cacao.Cacao
import com.walletconnect.android.keyserver.domain.IdentitiesInteractor
import com.walletconnect.push.common.data.jwt.EncodePushAuthDidJwtPayloadUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext

internal class RegisterIdentityAndReturnDidJwtUseCase(
    private val keyserverUrl: String,
    private val identitiesInteractor: IdentitiesInteractor,
) : RegisterIdentityAndReturnDidJwtUseCaseInterface {

    override suspend operator fun invoke(
        account: AccountId,
        metadataUrl: String,
        scopes: List<String>,
        onSign: (String) -> Cacao.Signature?,
        onFailure: (Throwable) -> Unit,
    ): Result<DidJwt> = supervisorScope {
        withContext(Dispatchers.IO) {
            identitiesInteractor.registerIdentity(account, keyserverUrl, onSign).getOrElse {
                onFailure(it)
                this.cancel()
            }
        }

        val joinedScope = scopes.joinToString(" ")
        val (identityPublicKey, identityPrivateKey) = identitiesInteractor.getIdentityKeyPair(account)

        return@supervisorScope encodeDidJwt(
            identityPrivateKey,
            EncodePushAuthDidJwtPayloadUseCase(metadataUrl, account, joinedScope),
            EncodeDidJwtPayloadUseCase.Params(identityPublicKey, keyserverUrl)
        )
    }
}

internal interface RegisterIdentityAndReturnDidJwtUseCaseInterface {

    suspend operator fun invoke(
        account: AccountId,
        metadataUrl: String,
        scopes: List<String>,
        onSign: (String) -> Cacao.Signature?,
        onFailure: (Throwable) -> Unit,
    ): Result<DidJwt>
}