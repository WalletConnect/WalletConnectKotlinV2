@file:JvmSynthetic

package com.walletconnect.notify.di

import com.walletconnect.notify.engine.responses.OnNotifyDeleteResponseUseCase
import com.walletconnect.notify.engine.responses.OnNotifySubscribeResponseUseCase
import com.walletconnect.notify.engine.responses.OnNotifyUpdateResponseUseCase
import com.walletconnect.notify.engine.responses.OnWatchSubscriptionsResponseUseCase
import org.koin.dsl.module

@JvmSynthetic
internal fun responseModule() = module {

    single {
        OnNotifySubscribeResponseUseCase(
            setActiveSubscriptionsUseCase = get(),
            findRequestedSubscriptionUseCase = get(),
            subscriptionRepository = get(),
            logger = get()
        )
    }

    single {
        OnNotifyUpdateResponseUseCase(
            setActiveSubscriptionsUseCase = get(),
            findRequestedSubscriptionUseCase = get()
        )
    }

    single {
        OnNotifyDeleteResponseUseCase(
            setActiveSubscriptionsUseCase = get(),
            jsonRpcInteractor = get(),
            notificationsRepository = get()
        )
    }

    single {
        OnWatchSubscriptionsResponseUseCase(
            setActiveSubscriptionsUseCase = get(),
            extractPublicKeysFromDidJsonUseCase = get(),
            watchSubscriptionsForEveryRegisteredAccountUseCase = get(),
            accountsRepository = get(),
            notifyServerUrl = get()
        )
    }
}