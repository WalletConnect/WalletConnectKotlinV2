@file:JvmSynthetic

package com.walletconnect.notify.engine.calls

import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.signing.cacao.Cacao
import com.walletconnect.notify.data.storage.RegisteredAccountsRepository
import com.walletconnect.notify.engine.domain.RegisterIdentityUseCase
import com.walletconnect.notify.engine.domain.WatchSubscriptionsUseCase
import kotlinx.coroutines.supervisorScope

internal class RegisterUseCase(
    private val registerIdentityUseCase: RegisterIdentityUseCase,
    private val registeredAccountsRepository: RegisteredAccountsRepository,
    private val watchSubscriptionsUseCase: WatchSubscriptionsUseCase,
) : RegisterUseCaseInterface {

    override suspend fun register(
        account: String,
        isLimited: Boolean,
        domain: String,
        onSign: (String) -> Cacao.Signature?,
        onSuccess: (String) -> Unit,
        onFailure: (Throwable) -> Unit,
    ) = supervisorScope {
        val accountId = AccountId(account)
        registerIdentityUseCase(
            accountId, isLimited, domain, onSign,
            onFailure = { error -> onFailure(error) },
            onSuccess = { identityPublicKey ->
                runCatching { registeredAccountsRepository.insertOrAbortAccount(accountId, identityPublicKey, isLimited) }.fold(
                    onFailure = { error -> onFailure(error) },
                    onSuccess = { watchSubscriptionsUseCase(accountId, onSuccess = { onSuccess(identityPublicKey.keyAsHex) }, onFailure = { error -> onFailure(error) }) }
                )
            }
        )
    }
}

internal interface RegisterUseCaseInterface {
    suspend fun register(
        account: String,
        isLimited: Boolean,
        domain: String,
        onSign: (String) -> Cacao.Signature?,
        onSuccess: (String) -> Unit,
        onFailure: (Throwable) -> Unit,
    )
}