@file:JvmSynthetic

package com.walletconnect.notify.di

import com.walletconnect.android.internal.common.di.AndroidCommonDITags
import com.walletconnect.notify.engine.requests.OnNotifyDeleteUseCase
import com.walletconnect.notify.engine.requests.OnNotifyMessageUseCase
import com.walletconnect.notify.engine.requests.OnSubscriptionsChangedUseCase
import org.koin.core.qualifier.named
import org.koin.dsl.module

@JvmSynthetic
internal fun requestModule() = module {

    single {
        OnNotifyMessageUseCase(
            jsonRpcInteractor = get(),
            messagesRepository = get(),
            subscriptionRepository = get(),
            fetchDidJwtInteractor = get(),
            metadataStorageRepository = get(),
            _moshi = get(named(AndroidCommonDITags.MOSHI)),
            logger = get()
        )
    }

    single {
        OnNotifyDeleteUseCase(
            jsonRpcInteractor = get(),
            subscriptionRepository = get(),
            metadataStorageRepository = get(),
            logger = get()
        )
    }

    single {
        OnSubscriptionsChangedUseCase(
            setActiveSubscriptionsUseCase = get(),
            fetchDidJwtInteractor = get(),
            extractPublicKeysFromDidJsonUseCase = get(),
            jsonRpcInteractor = get(),
            logger = get()
        )
    }
}