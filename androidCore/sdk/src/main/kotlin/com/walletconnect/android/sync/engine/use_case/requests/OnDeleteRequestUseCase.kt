package com.walletconnect.android.sync.engine.use_case.requests

import com.walletconnect.android.internal.common.model.WCRequest
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.android.sync.common.json_rpc.SyncParams
import com.walletconnect.android.sync.common.model.Events
import com.walletconnect.android.sync.common.model.SyncUpdate
import com.walletconnect.android.sync.storage.StoresStorageRepository
import com.walletconnect.foundation.util.Logger
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

internal class OnDeleteRequestUseCase(
    private val storesRepository: StoresStorageRepository,
    private val logger: Logger,
) {

    private val _events: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val events: SharedFlow<EngineEvent> = _events.asSharedFlow()

    suspend operator fun invoke(params: SyncParams.DeleteParams, request: WCRequest) {
        val (accountId, store) = runCatching { storesRepository.getAccountIdAndStoreByTopic(request.topic) }.getOrElse { error -> return logger.error(error) }
        logger.log("$store | ${request.id} DELETE -> ${params.key} ")

        // Return/finish when the value was already set
        runCatching { storesRepository.getStoreValue(accountId, store, params.key) }
            .onSuccess { (_, _, timestamp) -> if (request.id < timestamp) return logger.error("Received request is older than current state. Received: ${request.id} - stored: $timestamp") }
            .getOrElse { return logger.log("$accountId, store: $store, key: ${params.key}") }

        // Trigger on_syncUpdate event in onSuccess when the value was deleted
        runCatching { storesRepository.deleteStoreValue(accountId, store, params.key) }.fold(
            onFailure = { error -> logger.error(error) },
            onSuccess = { _events.emit(Events.OnSyncUpdate(accountId, store, SyncUpdate.SyncDelete(request.id, params.key))) },
        )
    }
}