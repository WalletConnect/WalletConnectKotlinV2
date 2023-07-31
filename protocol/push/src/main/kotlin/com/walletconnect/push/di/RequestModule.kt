package com.walletconnect.push.di

import com.walletconnect.push.engine.requests.OnPushDeleteUseCase
import com.walletconnect.push.engine.requests.OnPushMessageUseCase
import com.walletconnect.push.engine.requests.OnPushProposeUseCase
import org.koin.dsl.module

@JvmSynthetic
internal fun requestModule() = module {

    single {
        OnPushProposeUseCase(
            jsonRpcInteractor = get(),
            metadataStorageRepository = get(),
            proposalStorageRepository = get(),
            logger = get()
        )
    }

    single {
        OnPushMessageUseCase(
            jsonRpcInteractor = get(),
            messagesRepository = get()
        )
    }

    single {
        OnPushDeleteUseCase(
            jsonRpcInteractor = get(),
            subscriptionRepository = get(),
            logger = get()
        )
    }
}