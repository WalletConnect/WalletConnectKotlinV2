package com.walletconnect.sync.engine.use_case.calls

import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.scope
import com.walletconnect.sync.common.exception.validateAccountId
import com.walletconnect.sync.common.model.Store
import com.walletconnect.sync.storage.StoresStorageRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

internal class SetStoreValueUseCase(private val storesRepository: StoresStorageRepository) : SetUseCaseInterface {

    override fun set(accountId: AccountId, store: Store, key: String, value: String, onSuccess: (Boolean) -> Unit, onFailure: (Throwable) -> Unit) {
        scope.launch {
            supervisorScope {
                validateAccountId(accountId) { error -> return@supervisorScope onFailure(error) }

                // Return false in onSuccess when the value was already set
                runCatching { storesRepository.getStoreValue(accountId, store, key) }
                    .onSuccess { (_, currentValue) -> if (value == currentValue) return@supervisorScope onSuccess(false) }

                // Return true in onSuccess when the value was upserted
                runCatching { storesRepository.upsertStoreValue(accountId, store, key, value) }.fold(
                    onSuccess = { onSuccess(true) },
                    onFailure = { error -> onFailure(error) }
                )
            }
        }
    }
}

internal interface SetUseCaseInterface {
    fun set(accountId: AccountId, store: Store, key: String, value: String, onSuccess: (Boolean) -> Unit, onFailure: (Throwable) -> Unit)
}
