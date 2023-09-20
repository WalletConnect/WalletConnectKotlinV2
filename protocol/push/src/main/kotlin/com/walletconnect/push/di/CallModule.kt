package com.walletconnect.push.di

import com.walletconnect.push.engine.calls.ApproveSubscriptionRequestUseCase
import com.walletconnect.push.engine.calls.ApproveSubscriptionRequestUseCaseInterface
import com.walletconnect.push.engine.calls.DecryptMessageUseCase
import com.walletconnect.push.engine.calls.DecryptMessageUseCaseInterface
import com.walletconnect.push.engine.calls.DeleteMessageUseCase
import com.walletconnect.push.engine.calls.DeleteMessageUseCaseInterface
import com.walletconnect.push.engine.calls.DeleteSubscriptionUseCase
import com.walletconnect.push.engine.calls.DeleteSubscriptionUseCaseInterface
import com.walletconnect.push.engine.calls.EnableSyncUseCase
import com.walletconnect.push.engine.calls.EnableSyncUseCaseInterface
import com.walletconnect.push.engine.calls.GetListOfActiveSubscriptionsUseCase
import com.walletconnect.push.engine.calls.GetListOfActiveSubscriptionsUseCaseInterface
import com.walletconnect.push.engine.calls.GetListOfMessagesUseCase
import com.walletconnect.push.engine.calls.GetListOfMessagesUseCaseInterface
import com.walletconnect.push.engine.calls.RejectSubscriptionRequestUseCase
import com.walletconnect.push.engine.calls.RejectSubscriptionRequestUseCaseInterface
import com.walletconnect.push.engine.calls.SubscribeToDappUseCase
import com.walletconnect.push.engine.calls.SubscribeToDappUseCaseInterface
import com.walletconnect.push.engine.calls.UpdateSubscriptionRequestUseCase
import com.walletconnect.push.engine.calls.UpdateSubscriptionRequestUseCaseInterface
import org.koin.dsl.module

@JvmSynthetic
internal fun callModule() = module {

    single<SubscribeToDappUseCaseInterface> {
        SubscribeToDappUseCase(
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

    single<ApproveSubscriptionRequestUseCaseInterface> {
        ApproveSubscriptionRequestUseCase(
            subscribeUseCase = get(),
            proposalStorageRepository = get(),
            metadataStorageRepository = get(),
            crypto = get(),
            enginePushSubscriptionNotifier = get(),
            jsonRpcInteractor = get(),
        )
    }

    single<RejectSubscriptionRequestUseCaseInterface> {
        RejectSubscriptionRequestUseCase(
            proposalStorageRepository = get(),
            jsonRpcInteractor = get()
        )
    }

    single<UpdateSubscriptionRequestUseCaseInterface> {
        UpdateSubscriptionRequestUseCase(
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
        )
    }

    single<GetListOfActiveSubscriptionsUseCaseInterface> {
        GetListOfActiveSubscriptionsUseCase(
            subscriptionRepository = get(),
            metadataStorageRepository = get()
        )
    }

    single<GetListOfMessagesUseCaseInterface> {
        GetListOfMessagesUseCase(
            messagesRepository = get()
        )
    }
}