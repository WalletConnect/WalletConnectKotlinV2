package com.walletconnect.android.sync.engine.use_case.calls

import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.sync.common.exception.validateAccountId
import com.walletconnect.android.sync.common.model.StoreMap
import com.walletconnect.android.sync.storage.StoresStorageRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking

internal class GetStoresUseCase(private val storesRepository: StoresStorageRepository) : GetStoresUseCaseInterface {

    override fun getStores(accountId: AccountId): StoreMap? {
        validateAccountId(accountId) { error -> throw error }

        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        return runBlocking(scope.coroutineContext) {
            runCatching { storesRepository.getStoreMap(accountId) }.getOrNull()
        }
    }
}

internal interface GetStoresUseCaseInterface {
    fun getStores(accountId: AccountId): StoreMap?
}
