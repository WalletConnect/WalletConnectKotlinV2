@file:JvmSynthetic

package com.walletconnect.web3.inbox.sync.di

import com.walletconnect.utils.addDeserializerEntry
import com.walletconnect.utils.addSerializerEntry
import com.walletconnect.web3.inbox.json_rpc.Web3InboxMethods
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC
import org.koin.dsl.module

@JvmSynthetic
internal fun syncJsonRpcModule() = module {
    addSerializerEntry(Web3InboxRPC.Call.SyncUpdate::class)
    addDeserializerEntry(Web3InboxMethods.Call.SYNC_UPDATE, Web3InboxRPC.Call.SyncUpdate::class)
}