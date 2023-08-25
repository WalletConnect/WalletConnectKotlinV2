@file:JvmSynthetic

package com.walletconnect.notify.engine.domain

import com.walletconnect.android.internal.common.jwt.did.EncodeDidJwtPayloadUseCase
import com.walletconnect.android.internal.common.jwt.did.encodeDidJwt
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.model.DidJwt
import com.walletconnect.android.keyserver.domain.IdentitiesInteractor
import com.walletconnect.foundation.common.model.PrivateKey
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.notify.data.jwt.delete.EncodeDeleteRequestJwtUseCase
import com.walletconnect.notify.data.jwt.message.EncodeMessageReceiptJwtUseCase
import com.walletconnect.notify.data.jwt.subscription.EncodeSubscriptionRequestJwtUseCase
import com.walletconnect.notify.data.jwt.update.EncodeUpdateRequestJwtUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

internal class RegisterIdentityAndReturnDidJwtInteractor(
    private val keyserverUrl: String,
    private val identitiesInteractor: IdentitiesInteractor,
) {

    suspend fun subscriptionRequest(
        account: AccountId,
        authenticationKey: PublicKey,
        metadataUrl: String,
        scopes: List<String>,
        onFailure: (Throwable) -> Unit,
    ): Result<DidJwt> = registerIdentityAndReturnIdentityKeyPair(account, onFailure) { (identityPublicKey, identityPrivateKey) ->
        val concatenatedScopes = scopes.joinToString(SCOPES_DELIMITER)

        return@registerIdentityAndReturnIdentityKeyPair encodeDidJwt(
            identityPrivateKey,
            EncodeSubscriptionRequestJwtUseCase(metadataUrl, account, authenticationKey, concatenatedScopes),
            EncodeDidJwtPayloadUseCase.Params(identityPublicKey, keyserverUrl, expirySourceDuration = 30, expiryTimeUnit = TimeUnit.SECONDS)
        )
    }

    suspend fun deleteRequest(
        account: AccountId,
        metadataUrl: String,
        authenticationKey: PublicKey,
        onFailure: (Throwable) -> Unit,
    ): Result<DidJwt> = registerIdentityAndReturnIdentityKeyPair(account, onFailure) { (identityPublicKey, identityPrivateKey) ->

        return@registerIdentityAndReturnIdentityKeyPair encodeDidJwt(
            identityPrivateKey,
            EncodeDeleteRequestJwtUseCase(metadataUrl, authenticationKey),
            EncodeDidJwtPayloadUseCase.Params(identityPublicKey, keyserverUrl)
        )
    }

    suspend fun messageReceipt(
        account: AccountId,
        metadataUrl: String,
        authenticationKey: PublicKey,
        messageHash: String,
        onFailure: (Throwable) -> Unit,
    ): Result<DidJwt> = registerIdentityAndReturnIdentityKeyPair(account, onFailure) { (identityPublicKey, identityPrivateKey) ->

        return@registerIdentityAndReturnIdentityKeyPair encodeDidJwt(
            identityPrivateKey,
            EncodeMessageReceiptJwtUseCase(metadataUrl, authenticationKey, messageHash),
            EncodeDidJwtPayloadUseCase.Params(identityPublicKey, keyserverUrl)
        )
    }

    suspend fun updateRequest(
        account: AccountId,
        metadataUrl: String,
        authenticationKey: PublicKey,
        scopes: List<String>,
        onFailure: (Throwable) -> Unit,
    ): Result<DidJwt> = registerIdentityAndReturnIdentityKeyPair(account, onFailure) { (identityPublicKey, identityPrivateKey) ->
        val concatenatedScopes = scopes.joinToString(SCOPES_DELIMITER)

        return@registerIdentityAndReturnIdentityKeyPair encodeDidJwt(
            identityPrivateKey,
            EncodeUpdateRequestJwtUseCase(account, metadataUrl, authenticationKey, concatenatedScopes),
            EncodeDidJwtPayloadUseCase.Params(identityPublicKey, keyserverUrl, expirySourceDuration = 30, expiryTimeUnit = TimeUnit.SECONDS)
        )
    }

    @Suppress("MoveLambdaOutsideParentheses")
    private suspend fun registerIdentityAndReturnIdentityKeyPair(
        account: AccountId,
        onFailure: (Throwable) -> Unit,
        returnedKeys: suspend (Pair<PublicKey, PrivateKey>) -> Result<DidJwt>,
    ) = supervisorScope {
        withContext(Dispatchers.IO) {
            identitiesInteractor.registerIdentity(account, keyserverUrl, { null }).getOrElse {
                onFailure(it)
                this.cancel()
            }
        }

        returnedKeys(identitiesInteractor.getIdentityKeyPair(account))
    }

    companion object {
        const val SCOPES_DELIMITER = " "
    }
}