package com.walletconnect.sync.client

interface SyncInterface {
    interface SyncDelegate {}
    fun setSyncDelegate(delegate: SyncDelegate)
    fun initialize(params: Sync.Params.Init)
}