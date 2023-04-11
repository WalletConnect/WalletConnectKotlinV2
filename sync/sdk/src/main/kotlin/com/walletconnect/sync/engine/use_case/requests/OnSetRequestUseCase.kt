package com.walletconnect.sync.engine.use_case.requests

import com.walletconnect.android.internal.common.model.WCRequest
import com.walletconnect.sync.common.json_rpc.SyncParams
import com.walletconnect.sync.storage.AccountsStorageRepository
import com.walletconnect.sync.storage.StoresStorageRepository

internal class OnSetRequestUseCase(private val storesRepository: StoresStorageRepository, private val accountsRepository: AccountsStorageRepository) :
    RequestUseCase<SyncParams.SetParams> {
    override suspend operator fun invoke(params: SyncParams.SetParams, request: WCRequest) {
        TODO()

        // get account by request.topic
        // accountsRepository.getAccountByTopic() -> implement
//        validateAccountId(accountId) { error -> return@supervisorScope onFailure(error) }

        // Return/finish when the value was already set
//        runCatching { storesRepository.getStoreValue(accountId, store, key) }
//            .onSuccess { (_, currentValue) -> if (value == currentValue) return@supervisorScope }

        // Trigger on_syncUpdate event in onSuccess when the value was upserted
//        runCatching { storesRepository.upsertStoreValue(accountId, store, key, value) }.fold(
//            onSuccess = { /*trigger event*/ },
//            onFailure = { error -> onFailure(error) }
//        )

        // Send response over relay
    }
}