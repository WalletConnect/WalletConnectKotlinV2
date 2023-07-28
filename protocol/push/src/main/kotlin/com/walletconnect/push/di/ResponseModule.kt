package com.walletconnect.push.di

import com.walletconnect.push.engine.responses.OnPushSubscribeResponseUseCase
import com.walletconnect.push.engine.responses.OnPushUpdateResponseUseCase
import org.koin.dsl.module

@JvmSynthetic
internal fun responseModule() = module {

    single {
        OnPushSubscribeResponseUseCase(
            jsonRpcInteractor = get(),
            crypto = get(),
            subscriptionRepository = get(),
            metadataStorageRepository = get(),
            enginePushSubscriptionNotifier = get(),
            setSubscriptionWithSymmetricKeyToPushSubscriptionStoreUseCase = get(),
            logger = get(),
        )
    }

    single {
        OnPushUpdateResponseUseCase(
            subscriptionRepository = get()
        )
    }
}