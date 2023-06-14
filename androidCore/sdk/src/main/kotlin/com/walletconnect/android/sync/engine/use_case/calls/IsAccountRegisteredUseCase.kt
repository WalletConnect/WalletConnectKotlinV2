package com.walletconnect.android.sync.engine.use_case.calls

import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.sync.common.exception.validateAccountId
import com.walletconnect.android.sync.storage.AccountsStorageRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

internal class IsAccountRegisteredUseCase(private val accountsRepository: AccountsStorageRepository) :
    IsAccountRegisteredUseCaseInterface {

    override fun isRegistered(accountId: AccountId, onSuccess: (Boolean) -> Unit, onError: (Throwable) -> Unit) {
        scope.launch {
            supervisorScope {
                validateAccountId(accountId) { error -> return@supervisorScope onError(error) }

                // When account exists then return call onSuccess() and finish use case invocation
                runCatching { accountsRepository.doesAccountNotExists(accountId) }.fold(
                    onSuccess = { doesNotExists -> onSuccess(!doesNotExists) },
                    onFailure = { throwable -> onError(throwable) }
                )
            }
        }
    }
}

internal interface IsAccountRegisteredUseCaseInterface {
    fun isRegistered(accountId: AccountId, onSuccess: (Boolean) -> Unit, onError: (Throwable) -> Unit)
}

