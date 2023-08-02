@file:JvmSynthetic

package com.walletconnect.notify.di

import com.walletconnect.notify.engine.calls.DecryptMessageUseCase
import com.walletconnect.notify.engine.calls.DecryptMessageUseCaseInterface
import com.walletconnect.notify.engine.calls.DeleteMessageUseCase
import com.walletconnect.notify.engine.calls.DeleteMessageUseCaseInterface
import com.walletconnect.notify.engine.calls.DeleteSubscriptionUseCase
import com.walletconnect.notify.engine.calls.DeleteSubscriptionUseCaseInterface
import com.walletconnect.notify.engine.calls.EnableSyncUseCase
import com.walletconnect.notify.engine.calls.EnableSyncUseCaseInterface
import com.walletconnect.notify.engine.calls.GetListOfActiveSubscriptionsUseCase
import com.walletconnect.notify.engine.calls.GetListOfActiveSubscriptionsUseCaseInterface
import com.walletconnect.notify.engine.calls.GetListOfMessagesUseCase
import com.walletconnect.notify.engine.calls.GetListOfMessagesUseCaseInterface
import com.walletconnect.notify.engine.calls.GetNotificationTypesInterface
import com.walletconnect.notify.engine.calls.GetNotificationTypesUseCase
import com.walletconnect.notify.engine.calls.UpdateSubscriptionRequestUseCase
import com.walletconnect.notify.engine.calls.UpdateSubscriptionRequestUseCaseInterface
import org.koin.dsl.module

@JvmSynthetic
internal fun callModule() = module {

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
            deleteSubscriptionToNotifySubscriptionStore = get(),
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
            setupSyncInNotifyUseCase = get(),
            getMessagesFromHistoryUseCase = get()
        )
    }

    single<GetNotificationTypesInterface> {
        GetNotificationTypesUseCase()
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