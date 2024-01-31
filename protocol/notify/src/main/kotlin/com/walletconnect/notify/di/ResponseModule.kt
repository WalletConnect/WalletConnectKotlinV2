@file:JvmSynthetic

package com.walletconnect.notify.di

import com.walletconnect.android.internal.common.di.AndroidCommonDITags
import com.walletconnect.notify.engine.responses.OnDeleteResponseUseCase
import com.walletconnect.notify.engine.responses.OnGetNotificationsResponseUseCase
import com.walletconnect.notify.engine.responses.OnSubscribeResponseUseCase
import com.walletconnect.notify.engine.responses.OnUpdateResponseUseCase
import com.walletconnect.notify.engine.responses.OnWatchSubscriptionsResponseUseCase
import org.koin.core.qualifier.named
import org.koin.dsl.module

@JvmSynthetic
internal fun responseModule() = module {

    single {
        OnSubscribeResponseUseCase(
            setActiveSubscriptionsUseCase = get(),
            findRequestedSubscriptionUseCase = get(),
            subscriptionRepository = get(),
            jsonRpcInteractor = get(),
            logger = get()
        )
    }

    single {
        OnUpdateResponseUseCase(
            setActiveSubscriptionsUseCase = get(),
            findRequestedSubscriptionUseCase = get(),
            logger = get(named(AndroidCommonDITags.LOGGER))
        )
    }

    single {
        OnDeleteResponseUseCase(
            setActiveSubscriptionsUseCase = get(),
            jsonRpcInteractor = get(),
            notificationsRepository = get(),
            logger = get(named(AndroidCommonDITags.LOGGER))
        )
    }

    single {
        OnWatchSubscriptionsResponseUseCase(
            setActiveSubscriptionsUseCase = get(),
            extractPublicKeysFromDidJsonUseCase = get(),
            watchSubscriptionsForEveryRegisteredAccountUseCase = get(),
            accountsRepository = get(),
            notifyServerUrl = get(),
            logger = get(named(AndroidCommonDITags.LOGGER))
        )
    }

    single {
        OnGetNotificationsResponseUseCase(
            logger = get(named(AndroidCommonDITags.LOGGER)),
            metadataStorageRepository = get(),
            notificationsRepository = get(),
            subscriptionRepository = get()
        )
    }
}