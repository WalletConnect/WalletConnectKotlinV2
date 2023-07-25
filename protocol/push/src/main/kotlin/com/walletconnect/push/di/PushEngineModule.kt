@file:JvmSynthetic

package com.walletconnect.push.di

import com.walletconnect.android.internal.common.di.AndroidCommonDITags
import com.walletconnect.push.engine.PushWalletEngine
import com.walletconnect.push.engine.calls.ApproveUseCase
import com.walletconnect.push.engine.calls.ApproveUseCaseInterface
import com.walletconnect.push.engine.calls.RejectUseCase
import com.walletconnect.push.engine.calls.RejectUseCaseInterface
import com.walletconnect.push.engine.calls.SubscribeUseCase
import com.walletconnect.push.engine.calls.SubscribeUseCaseInterface
import com.walletconnect.push.engine.calls.UpdateUseCase
import com.walletconnect.push.engine.calls.UpdateUseCaseInterface
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

    single<ApproveUseCaseInterface> {
        ApproveUseCase(
            subscribeUseCase = get(),
            proposalStorageRepository = get(),
            metadataStorageRepository = get(),
            crypto = get(),
            enginePushSubscriptionNotifier = get(),
            jsonRpcInteractor = get(),
        )
    }

    single<RejectUseCaseInterface> {
        RejectUseCase(
            get(),
            get()
        )
    }

    single<UpdateUseCaseInterface> {
        UpdateUseCase(
            jsonRpcInteractor = get(),
            subscriptionRepository = get(),
            metadataStorageRepository = get(),
            registerIdentityAndReturnDidJwtUseCase = get()
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
            subscribeUserCase = get(),
            approveUseCase = get(),
            rejectUserCase = get(),
            updateUseCase = get()
        )
    }
}