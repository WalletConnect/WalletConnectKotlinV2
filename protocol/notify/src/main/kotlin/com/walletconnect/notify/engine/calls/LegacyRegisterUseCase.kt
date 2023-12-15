@file:JvmSynthetic

package com.walletconnect.notify.engine.calls

import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.signing.cacao.Cacao
import com.walletconnect.notify.data.storage.RegisteredAccountsRepository
import com.walletconnect.notify.engine.domain.RegisterIdentityUseCase
import com.walletconnect.notify.engine.domain.WatchSubscriptionsUseCase
import kotlinx.coroutines.supervisorScope

@Deprecated("Can be removed when the old registration flow is no longer supported.")
internal class LegacyRegisterUseCase(
    private val registerIdentityUseCase: RegisterIdentityUseCase,
    private val registeredAccountsRepository: RegisteredAccountsRepository,
    private val watchSubscriptionsUseCase: WatchSubscriptionsUseCase,
) : LegacyRegisterUseCaseInterface {

    @Deprecated("Can be removed when the old registration flow is no longer supported.")
    override suspend fun legacyRegister(
        account: String,
        isLimited: Boolean,
        domain: String,
        onSign: (String) -> Cacao.Signature?,
        onSuccess: (String) -> Unit,
        onFailure: (Throwable) -> Unit,
    ) = supervisorScope {
        val accountId = AccountId(account)
        registerIdentityUseCase(
            accountId, domain, isLimited, onSign,
            onFailure = { error -> onFailure(error) },
            onSuccess = { identityPublicKey ->
                runCatching { registeredAccountsRepository.insertOrIgnoreAccount(accountId, identityPublicKey, isLimited, if (isLimited) domain else null) }.fold(
                    onFailure = { error -> onFailure(error) },
                    onSuccess = { watchSubscriptionsUseCase(accountId, onSuccess = { onSuccess(identityPublicKey.keyAsHex) }, onFailure = { error -> onFailure(error) }) }
                )
            }
        )
    }
}

internal interface LegacyRegisterUseCaseInterface {
    @Deprecated("Can be removed when the old registration flow is no longer supported.")
    suspend fun legacyRegister(
        account: String,
        isLimited: Boolean,
        domain: String,
        onSign: (String) -> Cacao.Signature?,
        onSuccess: (String) -> Unit,
        onFailure: (Throwable) -> Unit,
    )
}