package com.walletconnect.sync.client

import com.walletconnect.sync.engine.domain.SyncEngine

class SyncProtocol : SyncInterface {
    private lateinit var syncEngine: SyncEngine

    companion object {
        val instance = SyncProtocol()
    }

    override fun setSyncDelegate(delegate: SyncInterface.SyncDelegate) {
        TODO("Not yet implemented")
    }

    override fun initialize(params: Sync.Params.Init) {
        TODO("Not yet implemented")
    }

    override fun getMessage(params: Sync.Params.GetMessage): Result<String> {
        TODO("Not yet implemented")
    }

    override fun register(params: Sync.Params.Register, onSuccess: () -> Unit, onError: (Sync.Model.Error) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun create(params: Sync.Params.Create, onSuccess: () -> Unit, onError: (Sync.Model.Error) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun set(params: Sync.Params.Set, onSuccess: () -> Unit, onError: (Sync.Model.Error) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun delete(params: Sync.Params.Delete, onSuccess: () -> Unit, onError: (Sync.Model.Error) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun getStores(params: Sync.Params.GetStores): Result<Sync.Type.StoreMap> {
        TODO("Not yet implemented")
    }
}