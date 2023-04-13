package com.walletconnect.sync.engine.use_case.requests

import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.WCRequest
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.utils.MONTH_IN_SECONDS
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.foundation.util.Logger
import com.walletconnect.sync.common.json_rpc.SyncParams
import com.walletconnect.sync.common.model.Events
import com.walletconnect.sync.common.model.SyncUpdate
import com.walletconnect.sync.storage.StoresStorageRepository
import kotlinx.coroutines.flow.MutableSharedFlow

internal class OnDeleteRequestUseCase(
    private val storesRepository: StoresStorageRepository,
    private val logger: Logger,
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
) {
    suspend operator fun invoke(params: SyncParams.DeleteParams, request: WCRequest, events: MutableSharedFlow<EngineEvent>) {
        val (accountId, store) = runCatching { storesRepository.getAccountIdAndStoreByTopic(request.topic) }.getOrElse { error -> return logger.error(error) }

        // Return/finish when the value was already set
        runCatching { storesRepository.getStoreValue(accountId, store, params.key) }
            .getOrElse { return logger.log("$accountId, store: $store, key: ${params.key}") }

        // Trigger on_syncUpdate event in onSuccess when the value was deleted
        runCatching { storesRepository.deleteStoreValue(accountId, store, params.key) }.fold(
            onFailure = { error -> logger.error(error) },
            onSuccess = {
                events.emit(Events.OnSyncUpdate(accountId, store, SyncUpdate.SyncDelete(request.id, params.key)))
                jsonRpcInteractor.respondWithSuccess(request, IrnParams(Tags.SESSION_DELETE_RESPONSE, Ttl(MONTH_IN_SECONDS)))
            },
        )
    }
}