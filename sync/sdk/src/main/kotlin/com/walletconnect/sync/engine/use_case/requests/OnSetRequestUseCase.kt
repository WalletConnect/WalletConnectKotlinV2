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
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

internal class OnSetRequestUseCase(
    private val storesRepository: StoresStorageRepository,
    private val logger: Logger,
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
) {

    private val _events: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val events: SharedFlow<EngineEvent> = _events.asSharedFlow()

    suspend operator fun invoke(params: SyncParams.SetParams, request: WCRequest) {
        val (accountId, store) = runCatching { storesRepository.getAccountIdAndStoreByTopic(request.topic) }.getOrElse { error -> return logger.error(error) }

        // Return/finish when the value was already set
        runCatching { storesRepository.getStoreValue(accountId, store, params.key) }
            .onSuccess { (_, currentValue) -> if (params.value == currentValue) return logger.log("$accountId, store: $store, key: ${params.key} == $currentValue") }

        // Trigger on_syncUpdate event in onSuccess when the value was upserted
        runCatching { storesRepository.upsertStoreValue(accountId, store, params.key, params.value) }.fold(
            onFailure = { error -> logger.error(error) },
            onSuccess = {
                _events.emit(Events.OnSyncUpdate(accountId, store, SyncUpdate.SyncSet(request.id, params.key, params.value)))
                jsonRpcInteractor.respondWithSuccess(request, IrnParams(Tags.SYNC_SET_RESPONSE, Ttl(MONTH_IN_SECONDS)))
            },
        )
    }
}