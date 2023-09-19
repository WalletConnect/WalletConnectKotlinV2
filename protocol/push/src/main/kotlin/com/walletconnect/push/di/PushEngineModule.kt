@file:JvmSynthetic

package com.walletconnect.push.di

import com.walletconnect.android.internal.common.di.AndroidCommonDITags
import com.walletconnect.push.engine.PushWalletEngine
import com.walletconnect.push.engine.domain.EnginePushSubscriptionNotifier
import com.walletconnect.push.engine.domain.RegisterIdentityAndReturnDidJwtUseCase
import com.walletconnect.push.engine.domain.RegisterIdentityAndReturnDidJwtUseCaseInterface
import org.koin.core.qualifier.named
import org.koin.dsl.module

@JvmSynthetic
internal fun walletEngineModule() = module {

    includes(
        callModule(),
        requestModule(),
        responseModule()
    )

    single {
        EnginePushSubscriptionNotifier()
    }

    single<RegisterIdentityAndReturnDidJwtUseCaseInterface> {
        RegisterIdentityAndReturnDidJwtUseCase(
            keyserverUrl = get(named(AndroidCommonDITags.KEYSERVER_URL)),
            identitiesInteractor = get()
        )
    }

    single {
        PushWalletEngine(
            jsonRpcInteractor = get(),
            pairingHandler = get(),
            logger = get(),
            syncClient = get(),
            onSyncUpdateEventUseCase = get(),
            subscribeUserCase = get(),
            approveUseCase = get(),
            rejectUserCase = get(),
            updateUseCase = get(),
            deleteSubscriptionUseCase = get(),
            deleteMessageUseCase = get(),
            decryptMessageUseCase = get(),
            enableSyncUseCase = get(),
            getListOfActiveSubscriptionsUseCase = get(),
            getListOfMessages = get(),
            onPushProposeUseCase = get(),
            onPushMessageUseCase = get(),
            onPushDeleteUseCase = get(),
            onPushSubscribeResponseUseCase = get(),
            onPushUpdateResponseUseCase = get()
        )
    }
}

