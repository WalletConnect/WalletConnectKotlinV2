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
}