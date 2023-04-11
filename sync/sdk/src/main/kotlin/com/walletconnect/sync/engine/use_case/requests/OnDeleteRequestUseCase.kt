package com.walletconnect.sync.engine.use_case.requests

import com.walletconnect.android.internal.common.model.WCRequest
import com.walletconnect.sync.common.json_rpc.SyncParams
import com.walletconnect.sync.storage.AccountsStorageRepository
import com.walletconnect.sync.storage.StoresStorageRepository

internal class OnDeleteRequestUseCase(private val storesRepository: StoresStorageRepository, private val accountsRepository: AccountsStorageRepository) :
    RequestUseCase<SyncParams.DeleteParams> {
    override suspend operator fun invoke(params: SyncParams.DeleteParams, request: WCRequest) {
        TODO()

        // get account by request.topic
        // accountsRepository.getAccountByTopic() -> implement
//        validateAccountId(accountId) { error -> return@supervisorScope onFailure(error) }

        // Return/finish when the value was not in storage
//        runCatching { storesRepository.getStoreValue(accountId, store, key) }.getOrElse { return@supervisorScope onSuccess(false) }
//
        // Trigger on_syncUpdate event in onSuccess when the value was deleted
//        runCatching { storesRepository.deleteStoreValue(accountId, store, key) }.fold(
//            onSuccess = { onSuccess(true) },
//            onFailure = { error -> onFailure(error) }
//        )

        // Send response over relay
    }
}