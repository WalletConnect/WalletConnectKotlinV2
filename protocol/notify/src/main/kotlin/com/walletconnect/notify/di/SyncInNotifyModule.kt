@file:JvmSynthetic

package com.walletconnect.notify.di

import com.walletconnect.android.internal.common.di.AndroidCommonDITags
import com.walletconnect.notify.engine.sync.use_case.GetMessagesFromHistoryUseCase
import com.walletconnect.notify.engine.sync.use_case.SetupSyncInNotifyUseCase
import com.walletconnect.notify.engine.sync.use_case.events.OnSubscriptionUpdateEventUseCase
import com.walletconnect.notify.engine.sync.use_case.events.OnSyncUpdateEventUseCase
import com.walletconnect.notify.engine.sync.use_case.requests.DeleteSubscriptionToNotifySubscriptionStoreUseCase
import com.walletconnect.notify.engine.sync.use_case.requests.SetSubscriptionWithSymmetricKeyToNotifySubscriptionStoreUseCase
import org.koin.core.qualifier.named
import org.koin.dsl.module

@JvmSynthetic
internal fun syncInNotifyModule() = module {
    single {
        OnSubscriptionUpdateEventUseCase(
            logger = get(),
            keyManagementRepository = get(),
            messagesRepository = get(),
            metadataStorageRepository = get(),
            historyInterface = get(),
            subscriptionRepository = get(),
            jsonRpcInteractor = get(),
            extractPublicKeysFromDidJsonUseCase = get(),
            _moshi = get(named(AndroidCommonDITags.MOSHI))
        )
    }
    single { OnSyncUpdateEventUseCase(get()) }

    single {
        SetSubscriptionWithSymmetricKeyToNotifySubscriptionStoreUseCase(
            logger = get(),
            syncClient = get(),
            _moshi = get(named(AndroidCommonDITags.MOSHI))
        )
    }

    single {
        DeleteSubscriptionToNotifySubscriptionStoreUseCase(
            logger = get(),
            syncClient = get(),
        )
    }

    single { SetupSyncInNotifyUseCase(get(), get()) }
    single { GetMessagesFromHistoryUseCase(get(),get(), get()) }
}