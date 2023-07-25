@file:JvmSynthetic

package com.walletconnect.push.di

import com.walletconnect.android.internal.common.di.AndroidCommonDITags
import com.walletconnect.push.engine.PushWalletEngine
import com.walletconnect.push.engine.calls.SubscribeUseCase
import com.walletconnect.push.engine.calls.SubscribeUseCaseInterface
import com.walletconnect.push.engine.domain.EnginePushSubscriptionNotifier
import org.koin.core.qualifier.named
import org.koin.dsl.module

@JvmSynthetic
internal fun walletEngineModule() = module {

    single {
        EnginePushSubscriptionNotifier()
    }

    single<SubscribeUseCaseInterface> {
        SubscribeUseCase(
            serializer = get(),
            jsonRpcInteractor = get(),
            extractPushConfigUseCase = get(),
            subscriptionRepository = get(),
            crypto = get(),
            explorerRepository = get(),
            metadataStorageRepository = get(),
            registerIdentityAndReturnDidJwt = get(),
            logger = get(),
        )
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
            codec = get(),
            logger = get(),
            syncClient = get(),
            onSyncUpdateEventUseCase = get(),
            setupSyncInPushUseCase = get(),
            setSubscriptionWithSymmetricKeyToPushSubscriptionStoreUseCase = get(),
            historyInterface = get(),
            getMessagesFromHistoryUseCase = get(),
            deleteSubscriptionToPushSubscriptionStoreUseCase = get(),
            subscribeUserCase = get()
        )
    }
}