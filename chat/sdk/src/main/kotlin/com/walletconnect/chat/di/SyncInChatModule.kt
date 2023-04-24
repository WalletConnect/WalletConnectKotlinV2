@file:JvmSynthetic

package com.walletconnect.chat.di

import com.walletconnect.chat.engine.sync.updates.OnChatInviteUpdateUseCase
import com.walletconnect.chat.engine.sync.updates.OnInviteKeysUpdateUseCase
import com.walletconnect.chat.engine.sync.updates.OnSyncUpdateUseCase
import com.walletconnect.chat.engine.sync.updates.OnThreadsUpdateUseCase
import org.koin.dsl.module

@JvmSynthetic
internal fun syncInChatModule() = module {
    single { OnChatInviteUpdateUseCase(get()) }
    single { OnInviteKeysUpdateUseCase() }
    single { OnThreadsUpdateUseCase() }
    single { OnSyncUpdateUseCase(get(), get(), get()) }
}