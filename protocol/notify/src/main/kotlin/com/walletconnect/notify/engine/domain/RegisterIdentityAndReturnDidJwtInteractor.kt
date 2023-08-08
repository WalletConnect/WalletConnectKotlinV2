@file:JvmSynthetic

package com.walletconnect.notify.engine.domain

import com.walletconnect.android.internal.common.jwt.did.EncodeDidJwtPayloadUseCase
import com.walletconnect.android.internal.common.jwt.did.encodeDidJwt
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.model.DidJwt
import com.walletconnect.android.internal.common.signing.cacao.Cacao
import com.walletconnect.android.keyserver.domain.IdentitiesInteractor
import com.walletconnect.foundation.common.model.PrivateKey
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.notify.data.jwt.subscription.EncodeSubscriptionRequestJwtUseCase
import com.walletconnect.notify.data.jwt.subscription.EncodeSubscriptionResponseJwtUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext

internal class RegisterIdentityAndReturnDidJwtInteractor(
    private val keyserverUrl: String,
    private val identitiesInteractor: IdentitiesInteractor,
) {

    suspend fun subscriptionRequest(
        account: AccountId,
        metadataUrl: String,
        scopes: List<String>,
        onSign: (String) -> Cacao.Signature?,
        onFailure: (Throwable) -> Unit,
    ): Result<DidJwt> = supervisorScope {
        val (identityPublicKey, identityPrivateKey) =  registerIdentityAndReturnIdentityKeyPair(account, onSign, onFailure)
        val joinedScope = scopes.joinToString(" ")

        return@supervisorScope encodeDidJwt(
            identityPrivateKey,
            EncodeSubscriptionRequestJwtUseCase(metadataUrl, account, joinedScope),
            EncodeDidJwtPayloadUseCase.Params(identityPublicKey, keyserverUrl)
        )
    }

    suspend fun subscriptionResponse(
        account: AccountId,
        metadataUrl: String,
        publicKey: PublicKey,
        onFailure: (Throwable) -> Unit,
        onSign: (String) -> Cacao.Signature? = { null },
    ): Result<DidJwt> = supervisorScope {
        val (identityPublicKey, identityPrivateKey) = registerIdentityAndReturnIdentityKeyPair(account, onSign, onFailure)

        return@supervisorScope encodeDidJwt(
            identityPrivateKey,
            EncodeSubscriptionResponseJwtUseCase(metadataUrl, publicKey),
            EncodeDidJwtPayloadUseCase.Params(identityPublicKey, keyserverUrl)
        )
    }

    private suspend fun registerIdentityAndReturnIdentityKeyPair(account: AccountId, onSign: (String) -> Cacao.Signature?, onFailure: (Throwable) -> Unit): Pair<PublicKey, PrivateKey> = withContext(Dispatchers.IO) {
        identitiesInteractor.registerIdentity(account, keyserverUrl, onSign).getOrElse {
            onFailure(it)
            this.cancel()
        }

        identitiesInteractor.getIdentityKeyPair(account)
    }
}