package com.walletconnect.sync.engine.use_case

import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.sync.common.model.StoreMap
import com.walletconnect.sync.storage.StoresStorageRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking

internal class GetStoresUseCase(private val storesRepository: StoresStorageRepository) : GetStoresUseCaseInterface {

    override fun getStores(accountId: AccountId): StoreMap? {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        return runBlocking(scope.coroutineContext) {
            runCatching { storesRepository.getStoreMap(accountId) }.getOrNull()
        }
    }
}

internal interface GetStoresUseCaseInterface {
    fun getStores(accountId: AccountId): StoreMap?
}
