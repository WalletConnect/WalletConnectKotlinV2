package com.walletconnect.android.sync.client

import com.walletconnect.android.Core
import com.walletconnect.android.sync.common.model.Events
import com.walletconnect.android.sync.common.model.StoreMap
import kotlinx.coroutines.flow.SharedFlow
import org.koin.core.KoinApplication


interface SyncInterface {
    fun initialize(koinApplication: KoinApplication, onError: (Core.Model.Error) -> Unit)

    val onSyncUpdateEvents: SharedFlow<Events.OnSyncUpdate>

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    fun getMessage(params: Sync.Params.GetMessage): String

    fun register(params: Sync.Params.Register, onSuccess: () -> Unit, onError: (Core.Model.Error) -> Unit)

    fun isRegistered(params: Sync.Params.IsRegistered, onSuccess: (Boolean) -> Unit, onError: (Core.Model.Error) -> Unit)

    fun create(params: Sync.Params.Create, onSuccess: () -> Unit, onError: (Core.Model.Error) -> Unit)

    fun set(params: Sync.Params.Set, onSuccess: (Boolean) -> Unit, onError: (Core.Model.Error) -> Unit)

    fun delete(params: Sync.Params.Delete, onSuccess: (Boolean) -> Unit, onError: (Core.Model.Error) -> Unit)

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    fun getStores(params: Sync.Params.GetStores): StoreMap?
}