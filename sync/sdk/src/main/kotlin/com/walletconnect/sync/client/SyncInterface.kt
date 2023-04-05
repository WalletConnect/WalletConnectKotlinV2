package com.walletconnect.sync.client

import com.walletconnect.android.internal.common.model.AccountId

interface SyncInterface {
    interface SyncDelegate {
        fun onSyncUpdate(onSyncUpdate: Sync.Events.OnSyncUpdate)

        fun onConnectionStateChange(state: Sync.Model.ConnectionState)

        fun onError(error: Sync.Model.Error)
    }

    fun setSyncDelegate(delegate: SyncDelegate)

    fun initialize(params: Sync.Params.Init)

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    fun getMessage(params: Sync.Params.GetMessage): Result<String> // discuss: Result or nullable String

    fun register(params: Sync.Params.Register, onSuccess: () -> Unit, onError: (Sync.Model.Error) -> Unit)

    fun create(params: Sync.Params.Create, onSuccess: () -> Unit, onError: (Sync.Model.Error) -> Unit)

    fun set(params: Sync.Params.Set, onSuccess: () -> Unit, onError: (Sync.Model.Error) -> Unit)

    fun delete(params: Sync.Params.Delete, onSuccess: () -> Unit, onError: (Sync.Model.Error) -> Unit)

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    fun getStores(params: Sync.Params.GetStores) : Result<Sync.Type.StoreMap> // discuss: Result or nullable String
}