@file:JvmSynthetic

package com.walletconnect.web3.inbox.di

import com.walletconnect.web3.inbox.chat.di.chatJsonRpcModule
import com.walletconnect.web3.inbox.push.di.pushJsonRpcModule
import com.walletconnect.web3.inbox.sync.di.syncJsonRpcModule
import org.koin.dsl.module

@JvmSynthetic
internal fun web3InboxJsonRpcModule() = module {
    includes(pushJsonRpcModule(), chatJsonRpcModule(), syncJsonRpcModule())
}