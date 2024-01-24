@file:JvmSynthetic

package com.walletconnect.notify.di

import com.walletconnect.android.internal.common.di.AndroidCommonDITags
import com.walletconnect.notify.common.NotifyServerUrl
import com.walletconnect.notify.engine.NotifyEngine
import com.walletconnect.notify.engine.domain.ExtractMetadataFromConfigUseCase
import com.walletconnect.notify.engine.domain.ExtractPublicKeysFromDidJsonUseCase
import com.walletconnect.notify.engine.domain.FetchDidJwtInteractor
import com.walletconnect.notify.engine.domain.FindRequestedSubscriptionUseCase
import com.walletconnect.notify.engine.domain.GenerateAppropriateUriUseCase
import com.walletconnect.notify.engine.domain.GetSelfKeyForWatchSubscriptionUseCase
import com.walletconnect.notify.engine.domain.SetActiveSubscriptionsUseCase
import com.walletconnect.notify.engine.domain.StopWatchingSubscriptionsUseCase
import com.walletconnect.notify.engine.domain.WatchSubscriptionsForEveryRegisteredAccountUseCase
import com.walletconnect.notify.engine.domain.WatchSubscriptionsUseCase
import org.koin.core.qualifier.named
import org.koin.dsl.module

@JvmSynthetic
internal fun engineModule() = module {

    single { NotifyServerUrl() }

    includes(
        callModule(),
        requestModule(),
        responseModule()
    )

    single {
        ExtractPublicKeysFromDidJsonUseCase(
            serializer = get(),
            generateAppropriateUri = get(),
        )
    }

    single {
        ExtractMetadataFromConfigUseCase(
            getNotifyConfigUseCase = get(),
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
        GetSelfKeyForWatchSubscriptionUseCase(
            keyManagementRepository = get(),
        )
    }

    single {
        WatchSubscriptionsUseCase(
            jsonRpcInteractor = get(),
            fetchDidJwtInteractor = get(),
            keyManagementRepository = get(),
            extractPublicKeysFromDidJsonUseCase = get(),
            notifyServerUrl = get(),
            registeredAccountsRepository = get(),
            getSelfKeyForWatchSubscriptionUseCase = get()
        )
    }

    single {
        StopWatchingSubscriptionsUseCase(
            jsonRpcInteractor = get(),
            registeredAccountsRepository = get()
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
            extractMetadataFromConfigUseCase = get(),
            metadataRepository = get(),
            jsonRpcInteractor = get(),
            keyStore = get(),
        )
    }

    single {
        FindRequestedSubscriptionUseCase(
            metadataStorageRepository = get()
        )
    }

    single {
        NotifyEngine(
            jsonRpcInteractor = get(),
            pairingHandler = get(),
            subscribeToDappUseCase = get(),
            updateUseCase = get(),
            deleteSubscriptionUseCase = get(),
            decryptMessageUseCase = get(named(AndroidCommonDITags.DECRYPT_NOTIFY_MESSAGE)),
            unregisterUseCase = get(),
            getNotificationTypesUseCase = get(),
            getListOfActiveSubscriptionsUseCase = get(),
            getNotificationHistoryUseCase = get(),
            onMessageUseCase = get(),
            onSubscribeResponseUseCase = get(),
            onUpdateResponseUseCase = get(),
            onDeleteResponseUseCase = get(),
            onWatchSubscriptionsResponseUseCase = get(),
            onGetNotificationsResponseUseCase = get(),
            watchSubscriptionsForEveryRegisteredAccountUseCase = get(),
            onSubscriptionsChangedUseCase = get(),
            isRegisteredUseCase = get(),
            prepareRegistrationUseCase = get(),
            registerUseCase = get(),
        )
    }
}

