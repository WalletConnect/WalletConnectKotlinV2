@file:JvmSynthetic

package com.walletconnect.push.wallet.di

import com.walletconnect.android.internal.common.di.AndroidCommonDITags
import com.walletconnect.push.wallet.engine.PushWalletEngine
import com.walletconnect.push.wallet.engine.domain.EnginePushSubscriptionNotifier
import org.koin.core.qualifier.named
import org.koin.dsl.module

@JvmSynthetic
internal fun walletEngineModule() = module {

    single {
        EnginePushSubscriptionNotifier()
    }

    single {
        PushWalletEngine(
            keyserverUrl = get(named(AndroidCommonDITags.KEYSERVER_URL)), get(), get(), get(), get(),
            proposalStorageRepository = get(),
            metadataStorageRepository = get(),
            messagesRepository = get(),
            enginePushSubscriptionNotifier = get(),
            identitiesInteractor = get(),
            serializer = get(),
            explorerRepository = get(),
            extractPushConfigUseCase = get(),
            codec = get(),
            logger = get(),
            syncClient = get(),
            onSyncUpdateEventUseCase = get(),
            setupSyncInPushUseCase = get(),
            setSubscriptionWithSymmetricKeyToPushSubscriptionStoreUseCase = get(),
            historyInterface = get(),
            getMessagesFromHistoryUseCase = get(),
            deleteSubscriptionToPushSubscriptionStoreUseCase = get()
        )
    }
}