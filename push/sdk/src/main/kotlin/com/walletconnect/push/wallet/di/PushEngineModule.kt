@file:JvmSynthetic

package com.walletconnect.push.wallet.di

import com.walletconnect.android.internal.common.di.AndroidCommonDITags
import com.walletconnect.push.wallet.engine.PushWalletEngine
import com.walletconnect.push.wallet.engine.domain.calls.ApproveUseCase
import com.walletconnect.push.wallet.engine.domain.calls.ApproveUseCaseInterface
import com.walletconnect.push.wallet.engine.domain.calls.DecryptMessageUseCase
import com.walletconnect.push.wallet.engine.domain.calls.DecryptMessageUseCaseInterface
import com.walletconnect.push.wallet.engine.domain.calls.DeleteMessageUseCase
import com.walletconnect.push.wallet.engine.domain.calls.DeleteMessageUseCaseInterface
import com.walletconnect.push.wallet.engine.domain.calls.DeleteSubscriptionUseCase
import com.walletconnect.push.wallet.engine.domain.calls.DeleteSubscriptionUseCaseInterface
import com.walletconnect.push.wallet.engine.domain.calls.GetListOfActiveSubscriptionsUseCase
import com.walletconnect.push.wallet.engine.domain.calls.GetListOfActiveSubscriptionsUseCaseInterface
import com.walletconnect.push.wallet.engine.domain.calls.GetListOfMessagesUseCase
import com.walletconnect.push.wallet.engine.domain.calls.GetListOfMessagesUseCaseInterface
import com.walletconnect.push.wallet.engine.domain.calls.RegisterIdentityAndReturnDidJwtUseCase
import com.walletconnect.push.wallet.engine.domain.calls.RejectUseCase
import com.walletconnect.push.wallet.engine.domain.calls.RejectUseCaseInterface
import com.walletconnect.push.wallet.engine.domain.calls.SubscribeToDappUseCase
import com.walletconnect.push.wallet.engine.domain.calls.SubscribeToDappUseCaseInterface
import com.walletconnect.push.wallet.engine.domain.calls.UpdateUseCase
import com.walletconnect.push.wallet.engine.domain.calls.UpdateUseCaseInterface
import com.walletconnect.push.wallet.engine.domain.requests.OnPushRequestUseCase
import com.walletconnect.push.wallet.engine.domain.requests.OnPushRequestUseCaseInterface
import org.koin.core.qualifier.named
import org.koin.dsl.module

@JvmSynthetic
internal fun walletEngineModule() = module {

    single<SubscribeToDappUseCaseInterface> {
        SubscribeToDappUseCase(
            serializer = get(),
            crypto = get(),
            explorerRepository = get(),
            registerIdentityAndReturnDidJwtUseCase = get(),
            jsonRpcInteractor = get(),
            subscriptionStorageRepository = get(),
            extractPushConfigUseCase = get()
        )
    }

    single<ApproveUseCaseInterface> {
        ApproveUseCase(
            subscriptionStorageRepository = get(),
            registerIdentityAndReturnDidJwtUseCase = get(),
            crypto = get(),
            jsonRpcInteractor = get()
        )
    }

    single<RejectUseCaseInterface> {
        RejectUseCase(
            subscriptionStorageRepository = get(),
            jsonRpcInteractor = get()
        )
    }

    single<UpdateUseCaseInterface> {
        UpdateUseCase(
            subscriptionStorageRepository = get(),
            jsonRpcInteractor = get(),
            registerIdentityAndReturnDidJwt = get()
        )
    }

    single<DeleteSubscriptionUseCaseInterface> {
        DeleteSubscriptionUseCase(
            subscriptionStorageRepository = get(),
            jsonRpcInteractor = get(),
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
            serializer = get(),
            codec = get()
        )
    }

    single<GetListOfActiveSubscriptionsUseCaseInterface> {
        GetListOfActiveSubscriptionsUseCase(
            subscriptionStorageRepository = get()
        )
    }

    single<GetListOfMessagesUseCaseInterface> {
        GetListOfMessagesUseCase(
            messagesRepository = get()
        )
    }

    single<OnPushRequestUseCaseInterface> {
        OnPushRequestUseCase(subscriptionStorageRepository = get(), jsonRpcInteractor = get())
    }

    single {
        RegisterIdentityAndReturnDidJwtUseCase(
            identitiesInteractor = get(),
            keyserverUrl = get(named(AndroidCommonDITags.KEYSERVER_URL))
        )
    }


    single {
        PushWalletEngine(
            jsonRpcInteractor = get(),
            crypto = get(),
            pairingHandler = get(),
            subscriptionStorageRepository = get(),
            messagesRepository = get(),
            extractPushConfigUseCase = get(),
            subscriptToDappUseCase = get(),
            approveUseCase = get(),
            rejectUseCase = get(),
            updateUseCase = get(),
            deleteSubscriptionUseCaseInterface = get(),
            deleteMessageUseCaseInterface = get(),
            decryptMessageUseCase = get(),
            getListOfActiveSubscriptionsUseCaseInterface = get(),
            getListOfMessagesUseCaseInterface = get(),
            onPushRequestUseCaseInterface = get()
        )
    }
}