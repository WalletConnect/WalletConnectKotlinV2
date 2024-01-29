@file:JvmSynthetic

package com.walletconnect.notify.di

import com.walletconnect.android.internal.common.di.AndroidCommonDITags
import com.walletconnect.notify.engine.requests.OnMessageUseCase
import com.walletconnect.notify.engine.requests.OnSubscriptionsChangedUseCase
import org.koin.core.qualifier.named
import org.koin.dsl.module

@JvmSynthetic
internal fun requestModule() = module {

    single {
        OnMessageUseCase(
            jsonRpcInteractor = get(),
            notificationsRepository = get(),
            subscriptionRepository = get(),
            fetchDidJwtInteractor = get(),
            metadataStorageRepository = get(),
            logger = get(named(AndroidCommonDITags.LOGGER)),
        )
    }

    single {
        OnSubscriptionsChangedUseCase(
            setActiveSubscriptionsUseCase = get(),
            fetchDidJwtInteractor = get(),
            extractPublicKeysFromDidJsonUseCase = get(),
            jsonRpcInteractor = get(),
            logger = get(named(AndroidCommonDITags.LOGGER)),
            notifyServerUrl = get(),
            registeredAccountsRepository = get(),
            watchSubscriptionsForEveryRegisteredAccountUseCase = get(),
        )
    }
}