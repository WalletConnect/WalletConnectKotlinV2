@file:JvmSynthetic

package com.walletconnect.web3.inbox.sync.di

import com.walletconnect.web3.inbox.sync.event.OnSyncUpdateEventUseCase
import com.walletconnect.web3.inbox.sync.event.SyncEventHandler
import org.koin.dsl.module

@JvmSynthetic
internal fun syncProxyModule(
) = module {
    single { OnSyncUpdateEventUseCase(onSyncUpdateChatEventUseCase = get(), onSyncUpdatePushEventUseCase = get()) }
    single { SyncEventHandler(syncClient = get(), jsonRpcInteractor = get(), onSyncUpdateEventUseCase = get()) }
}
