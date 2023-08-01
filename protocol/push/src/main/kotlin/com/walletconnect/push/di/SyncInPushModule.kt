@file:JvmSynthetic

package com.walletconnect.push.di

import com.walletconnect.android.internal.common.di.AndroidCommonDITags
import com.walletconnect.push.engine.sync.use_case.GetMessagesFromHistoryUseCase
import com.walletconnect.push.engine.sync.use_case.SetupSyncInPushUseCase
import com.walletconnect.push.engine.sync.use_case.events.OnSubscriptionUpdateEventUseCase
import com.walletconnect.push.engine.sync.use_case.events.OnSyncUpdateEventUseCase
import com.walletconnect.push.engine.sync.use_case.requests.DeleteSubscriptionToPushSubscriptionStoreUseCase
import com.walletconnect.push.engine.sync.use_case.requests.SetSubscriptionWithSymmetricKeyToPushSubscriptionStoreUseCase
import org.koin.core.qualifier.named
import org.koin.dsl.module

@JvmSynthetic
internal fun syncInPushModule() = module {
    single {
        OnSubscriptionUpdateEventUseCase(
            logger = get(),
            keyManagementRepository = get(),
            messagesRepository = get(),
            metadataStorageRepository = get(),
            historyInterface = get(),
            subscriptionRepository = get(),
            jsonRpcInteractor = get(),
            _moshi = get(named(AndroidCommonDITags.MOSHI))
        )
    }
    single { OnSyncUpdateEventUseCase(get()) }

    single {
        SetSubscriptionWithSymmetricKeyToPushSubscriptionStoreUseCase(
            logger = get(),
            syncClient = get(),
            _moshi = get(named(AndroidCommonDITags.MOSHI))
        )
    }

    single {
        DeleteSubscriptionToPushSubscriptionStoreUseCase(
            logger = get(),
            syncClient = get(),
        )
    }

    single { GetMessagesFromHistoryUseCase(get(),get(), get()) }
    single { SetupSyncInPushUseCase(get(), get(), get()) }
}