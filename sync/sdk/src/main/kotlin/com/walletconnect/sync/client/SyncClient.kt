package com.walletconnect.sync.client


object SyncClient : SyncInterface by SyncProtocol.instance {
    interface SyncDelegate : SyncInterface.SyncDelegate
}