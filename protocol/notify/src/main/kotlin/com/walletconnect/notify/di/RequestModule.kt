@file:JvmSynthetic

package com.walletconnect.notify.di

import com.walletconnect.notify.engine.requests.OnNotifyDeleteUseCase
import com.walletconnect.notify.engine.requests.OnNotifyMessageUseCase
import org.koin.dsl.module

@JvmSynthetic
internal fun requestModule() = module {

    single {
        OnNotifyMessageUseCase(
            jsonRpcInteractor = get(),
            messagesRepository = get()
        )
    }

    single {
        OnNotifyDeleteUseCase(
            jsonRpcInteractor = get(),
            subscriptionRepository = get(),
            logger = get()
        )
    }
}