@file:JvmSynthetic

package com.walletconnect.push.di

import com.walletconnect.android.internal.common.di.AndroidCommonDITags
import com.walletconnect.push.engine.PushWalletEngine
import com.walletconnect.push.engine.calls.ApproveUseCase
import com.walletconnect.push.engine.calls.ApproveUseCaseInterface
import com.walletconnect.push.engine.calls.DecryptMessageUseCase
import com.walletconnect.push.engine.calls.DecryptMessageUseCaseInterface
import com.walletconnect.push.engine.calls.DeleteMessageUseCase
import com.walletconnect.push.engine.calls.DeleteMessageUseCaseInterface
import com.walletconnect.push.engine.calls.DeleteSubscriptionUseCase
import com.walletconnect.push.engine.calls.DeleteSubscriptionUseCaseInterface
import com.walletconnect.push.engine.calls.EnableSyncUseCase
import com.walletconnect.push.engine.calls.EnableSyncUseCaseInterface
import com.walletconnect.push.engine.calls.RejectUseCase
import com.walletconnect.push.engine.calls.RejectUseCaseInterface
import com.walletconnect.push.engine.calls.SubscribeUseCase
import com.walletconnect.push.engine.calls.SubscribeUseCaseInterface
import com.walletconnect.push.engine.calls.UpdateUseCase
import com.walletconnect.push.engine.calls.UpdateUseCaseInterface
import com.walletconnect.push.engine.domain.EnginePushSubscriptionNotifier
import com.walletconnect.push.engine.domain.RegisterIdentityAndReturnDidJwtUseCase
import com.walletconnect.push.engine.domain.RegisterIdentityAndReturnDidJwtUseCaseInterface
import org.koin.core.qualifier.named
import org.koin.dsl.module

@JvmSynthetic
internal fun walletEngineModule() = module {

    single {
        EnginePushSubscriptionNotifier()
    }

    single<RegisterIdentityAndReturnDidJwtUseCaseInterface> {
        RegisterIdentityAndReturnDidJwtUseCase(
            keyserverUrl = get(named(AndroidCommonDITags.KEYSERVER_URL)),
            identitiesInteractor = get()
        )
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
            proposalStorageRepository = get(),
            jsonRpcInteractor = get()
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

    single<DeleteSubscriptionUseCaseInterface> {
        DeleteSubscriptionUseCase(
            jsonRpcInteractor = get(),
            subscriptionRepository = get(),
            messagesRepository = get(),
            deleteSubscriptionToPushSubscriptionStore = get(),
            logger = get()
        )
    }

    single<DeleteMessageUseCaseInterface> {
        DeleteMessageUseCase(
            messagesRepository = get()
        )
    }

    single<DecryptMessageUseCaseInterface> {
        DecryptMessageUseCase(
            codec = get(),
            serializer = get()
        )
    }

    single<EnableSyncUseCaseInterface> {
        EnableSyncUseCase(
            setupSyncInPushUseCase = get(),
            getMessagesFromHistoryUseCase = get()
        )
    }

    single {
        PushWalletEngine(
            jsonRpcInteractor = get(),
            crypto = get(),
            pairingHandler = get(),
            subscriptionRepository = get(),
            proposalStorageRepository = get(),
            metadataStorageRepository = get(),
            messagesRepository = get(),
            enginePushSubscriptionNotifier = get(),
            logger = get(),
            syncClient = get(),
            onSyncUpdateEventUseCase = get(),
            setSubscriptionWithSymmetricKeyToPushSubscriptionStoreUseCase = get(),
            historyInterface = get(),
            subscribeUserCase = get(),
            approveUseCase = get(),
            rejectUserCase = get(),
            updateUseCase = get(),
            deleteSubscriptionUseCase = get(),
            deleteMessageUseCase = get(),
            decryptMessageUseCase = get(),
            enableSyncUseCase = get()
        )
    }
}