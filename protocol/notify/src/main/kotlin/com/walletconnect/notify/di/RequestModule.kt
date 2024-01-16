@file:JvmSynthetic

package com.walletconnect.notify.di

import com.walletconnect.notify.engine.requests.OnNotifyMessageUseCase
import com.walletconnect.notify.engine.requests.OnSubscriptionsChangedUseCase
import org.koin.dsl.module

@JvmSynthetic
internal fun requestModule() = module {

    single {
        OnNotifyMessageUseCase(
            jsonRpcInteractor = get(),
            notificationsRepository = get(),
            subscriptionRepository = get(),
            fetchDidJwtInteractor = get(),
            metadataStorageRepository = get(),
        )
    }

    single {
        OnSubscriptionsChangedUseCase(
            setActiveSubscriptionsUseCase = get(),
            fetchDidJwtInteractor = get(),
            extractPublicKeysFromDidJsonUseCase = get(),
            jsonRpcInteractor = get(),
            logger = get(),
            notifyServerUrl = get(),
            registeredAccountsRepository = get(),
            watchSubscriptionsForEveryRegisteredAccountUseCase = get(),
        )
    }
}