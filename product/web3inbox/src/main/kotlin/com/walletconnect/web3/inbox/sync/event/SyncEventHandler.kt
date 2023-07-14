package com.walletconnect.web3.inbox.sync.event

import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.sync.client.SyncInterface
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class SyncEventHandler(
    private val syncClient: SyncInterface,
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val onSyncUpdateEventUseCase: OnSyncUpdateEventUseCase,
) {
    private var syncEventUpdatesJob: Job? = null

    fun setup() {
        jsonRpcInteractor.isConnectionAvailable
            .filter { isAvailable: Boolean -> isAvailable }
            .onEach {

                if (syncEventUpdatesJob == null) {
                    syncEventUpdatesJob = collectSyncUpdateEvents(syncClient)
                }
            }
            .launchIn(scope)
    }

    private fun collectSyncUpdateEvents(syncClient: SyncInterface): Job = syncClient.onSyncUpdateEvents
        .onEach { event -> onSyncUpdateEventUseCase(event) }
        .launchIn(scope)
}