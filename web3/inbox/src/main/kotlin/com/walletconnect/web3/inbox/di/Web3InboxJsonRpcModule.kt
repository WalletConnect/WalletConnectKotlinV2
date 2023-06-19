@file:JvmSynthetic

package com.walletconnect.web3.inbox.di

import com.walletconnect.utils.addDeserializerEntry
import com.walletconnect.utils.addSerializerEntry
import com.walletconnect.web3.inbox.chat.di.chatJsonRpcModule
import com.walletconnect.web3.inbox.json_rpc.Web3InboxMethods
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC
import com.walletconnect.web3.inbox.push.di.pushJsonRpcModule
import org.koin.dsl.module

@JvmSynthetic
internal fun web3InboxJsonRpcModule() = module {
    includes(pushJsonRpcModule(), chatJsonRpcModule())

    addSerializerEntry(Web3InboxRPC.Call.SyncUpdate::class)
    addDeserializerEntry(Web3InboxMethods.Call.SYNC_UPDATE, Web3InboxRPC.Call.SyncUpdate::class)
}