@file:JvmSynthetic

package com.walletconnect.notify.di

import com.walletconnect.android.internal.common.di.AndroidCommonDITags
import com.walletconnect.notify.engine.requests.OnNotifyDeleteUseCase
import com.walletconnect.notify.engine.requests.OnNotifyMessageUseCase
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
}