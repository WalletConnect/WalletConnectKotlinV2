package com.walletconnect.sync.engine.use_case.calls

import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.scope
import com.walletconnect.sync.common.exception.validateAccountId
import com.walletconnect.sync.common.model.Store
import com.walletconnect.sync.engine.use_case.requests.outgoing.SendDeleteRequestUseCase
import com.walletconnect.sync.storage.StoresStorageRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

internal class DeleteStoreValueUseCase(private val storesRepository: StoresStorageRepository, private val sendDeleteRequestUseCase: SendDeleteRequestUseCase) : DeleteUseCaseInterface {

    // https://github.com/WalletConnect/WalletConnectKotlinV2/issues/800 -> update params to have StoreKey and StoreValue
    override fun delete(accountId: AccountId, store: Store, key: String, onSuccess: (Boolean) -> Unit, onFailure: (Throwable) -> Unit) {
        scope.launch {
            supervisorScope {
                validateAccountId(accountId) { error -> return@supervisorScope onFailure(error) }

                // Return false in onSuccess when the value was not in storage
                runCatching { storesRepository.getStoreValue(accountId, store, key) }.getOrElse { return@supervisorScope onSuccess(false) }

                // Return true in onSuccess when the value was deleted
                runCatching { storesRepository.deleteStoreValue(accountId, store, key) }.fold(
                    onSuccess = { sendDeleteRequestUseCase(key, accountId, store, onSuccess = { onSuccess(true) }, onError = onFailure) },
                    onFailure = { error -> onFailure(error) }
                )
            }
        }
    }
}

internal interface DeleteUseCaseInterface {
    fun delete(accountId: AccountId, store: Store, key: String, onSuccess: (Boolean) -> Unit, onFailure: (Throwable) -> Unit)
}
