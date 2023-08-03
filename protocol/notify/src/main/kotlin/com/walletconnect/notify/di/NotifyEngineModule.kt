@file:JvmSynthetic

package com.walletconnect.notify.di

import com.walletconnect.android.internal.common.di.AndroidCommonDITags
import com.walletconnect.notify.engine.NotifyEngine
import com.walletconnect.notify.engine.domain.EngineNotifySubscriptionNotifier
import com.walletconnect.notify.engine.domain.ExtractNotifyConfigUseCase
import com.walletconnect.notify.engine.domain.RegisterIdentityAndReturnDidJwtUseCase
import com.walletconnect.notify.engine.domain.RegisterIdentityAndReturnDidJwtUseCaseInterface
import org.koin.core.qualifier.named
import org.koin.dsl.module

@JvmSynthetic
internal fun engineModule() = module {

    includes(
        callModule(),
        requestModule(),
        responseModule()
    )

    single {
        EngineNotifySubscriptionNotifier()
    }

    single {
        ExtractNotifyConfigUseCase(serializer = get())
    }

    single<RegisterIdentityAndReturnDidJwtUseCaseInterface> {
        RegisterIdentityAndReturnDidJwtUseCase(
            keyserverUrl = get(named(AndroidCommonDITags.KEYSERVER_URL)),
            identitiesInteractor = get()
        )
    }

    single {
        NotifyEngine(
            jsonRpcInteractor = get(),
            pairingHandler = get(),
            logger = get(),
            syncClient = get(),
            onSyncUpdateEventUseCase = get(),
            historyInterface = get(),
            subscribeToDappUseCase = get(),
            updateUseCase = get(),
            deleteSubscriptionUseCase = get(),
            deleteMessageUseCase = get(),
            decryptMessageUseCase = get(),
            enableSyncUseCase = get(),
            getNotificationTypesUseCase = get(),
            getListOfActiveSubscriptionsUseCase = get(),
            getListOfMessages = get(),
            onNotifyMessageUseCase = get(),
            onNotifyDeleteUseCase = get(),
            onNotifySubscribeResponseUseCase = get(),
            onNotifyUpdateResponseUseCase = get()
        )
    }
}

