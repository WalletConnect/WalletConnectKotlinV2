package com.walletconnect.android.sync.engine.use_case.calls

import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.sync.common.exception.validateAccountId
import com.walletconnect.android.sync.common.model.Store
import com.walletconnect.android.sync.storage.StoresStorageRepository
import com.walletconnect.foundation.common.model.Topic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking

internal class GetStoreTopicUseCase(private val storesRepository: StoresStorageRepository) : GetStoreTopicUseCaseInterface {

    override fun getStoreTopic(accountId: AccountId, store: Store): Topic? {
        validateAccountId(accountId) { error -> throw error }

        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        return runBlocking(scope.coroutineContext) {
            runCatching { Topic(storesRepository.getStoreTopic(accountId, store)) }.getOrNull()
        }
    }
}

internal interface GetStoreTopicUseCaseInterface {
    fun getStoreTopic(accountId: AccountId, store: Store): Topic?
}
