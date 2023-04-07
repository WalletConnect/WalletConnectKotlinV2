package com.walletconnect.sync.engine.use_case

import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.scope
import com.walletconnect.sync.common.model.Store
import com.walletconnect.sync.storage.StoresStorageRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

internal class DeleteUseCase(private val storesRepository: StoresStorageRepository) : DeleteUseCaseInterface {

    override fun delete(accountId: AccountId, store: Store, key: String, onSuccess: (Boolean) -> Unit, onFailure: (Throwable) -> Unit) {
        scope.launch {
            supervisorScope {
                // Return false in onSuccess when the value was not in storage
                runCatching { storesRepository.getStoreValue(accountId, store, key) }.getOrElse { return@supervisorScope onSuccess(false) }

                // Return true in onSuccess when the value was deleted
                runCatching { storesRepository.deleteStoreValue(accountId, store, key) }.fold(
                    onSuccess = { onSuccess(true) },
                    onFailure = { error -> onFailure(error) }
                )
            }
        }
    }
}

internal interface DeleteUseCaseInterface {
    fun delete(accountId: AccountId, store: Store, key: String, onSuccess: (Boolean) -> Unit, onFailure: (Throwable) -> Unit)
}
