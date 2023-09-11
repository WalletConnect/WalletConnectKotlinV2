@file:JvmSynthetic

package com.walletconnect.notify.di

import com.walletconnect.android.internal.common.di.AndroidCommonDITags
import com.walletconnect.notify.engine.NotifyEngine
import com.walletconnect.notify.engine.domain.EngineNotifySubscriptionNotifier
import com.walletconnect.notify.engine.domain.ExtractMetadataFromConfigUseCase
import com.walletconnect.notify.engine.domain.ExtractPublicKeysFromDidJsonUseCase
import com.walletconnect.notify.engine.domain.FetchDidJwtInteractor
import com.walletconnect.notify.engine.domain.GenerateAppropriateUriUseCase
import com.walletconnect.notify.engine.domain.RegisterIdentityUseCase
import com.walletconnect.notify.engine.domain.SetActiveSubscriptionsUseCase
import com.walletconnect.notify.engine.domain.WatchSubscriptionsForEveryRegisteredAccountUseCase
import com.walletconnect.notify.engine.domain.WatchSubscriptionsUseCase
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
        ExtractPublicKeysFromDidJsonUseCase(
            serializer = get(),
            generateAppropriateUri = get(),
            logger = get()
        )
    }

    single {
        ExtractMetadataFromConfigUseCase(
            serializer = get(),
            generateAppropriateUri = get(),
            logger = get()
        )
    }

    single {
        GenerateAppropriateUriUseCase()
    }

    single {
        FetchDidJwtInteractor(
            keyserverUrl = get(named(AndroidCommonDITags.KEYSERVER_URL)),
            identitiesInteractor = get()
        )
    }

    single {
        WatchSubscriptionsUseCase(
            jsonRpcInteractor = get(),
            fetchDidJwtInteractor = get(),
            keyManagementRepository = get(),
            extractPublicKeysFromDidJsonUseCase = get(),
            logger = get()
        )
    }

    single {
        WatchSubscriptionsForEveryRegisteredAccountUseCase(
            watchSubscriptionsUseCase = get(), registeredAccountsRepository = get(), logger = get()
        )
    }

    single {
        SetActiveSubscriptionsUseCase(
            subscriptionRepository = get(),
            extractPublicKeysFromDidJsonUseCase = get(),
            extractMetadataFromConfigUseCase = get(),
            metadataRepository = get(),
            jsonRpcInteractor = get(),
            logger = get(),
            keyStore = get()
        )
    }

    single {
        RegisterIdentityUseCase(
            identitiesInteractor = get(),
            identityServerUrl = get(named(AndroidCommonDITags.KEYSERVER_URL))
        )
    }

    single {
        NotifyEngine(
            jsonRpcInteractor = get(),
            pairingHandler = get(),
            logger = get(),
            syncClient = get(),
            archiveInterface = get(),
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
            onNotifyUpdateResponseUseCase = get(),
            onWatchSubscriptionsResponseUseCase = get(),
            watchSubscriptionsForEveryRegisteredAccountUseCase = get(),
            onSubscriptionsChangedUseCase = get()
        )
    }
}

