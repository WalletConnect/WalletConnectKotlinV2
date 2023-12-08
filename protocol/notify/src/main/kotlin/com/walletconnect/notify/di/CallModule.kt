@file:JvmSynthetic

package com.walletconnect.notify.di

import com.walletconnect.android.internal.common.di.AndroidCommonDITags
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.push.notifications.DecryptMessageUseCaseInterface
import com.walletconnect.notify.engine.calls.DecryptNotifyMessageUseCase
import com.walletconnect.notify.engine.calls.DeleteNotificationUseCase
import com.walletconnect.notify.engine.calls.DeleteNotificationUseCaseInterface
import com.walletconnect.notify.engine.calls.DeleteSubscriptionUseCase
import com.walletconnect.notify.engine.calls.DeleteSubscriptionUseCaseInterface
import com.walletconnect.notify.engine.calls.GetListOfActiveSubscriptionsUseCase
import com.walletconnect.notify.engine.calls.GetListOfActiveSubscriptionsUseCaseInterface
import com.walletconnect.notify.engine.calls.GetListOfNotificationsUseCase
import com.walletconnect.notify.engine.calls.GetListOfNotificationsUseCaseInterface
import com.walletconnect.notify.engine.calls.GetNotificationTypesUseCase
import com.walletconnect.notify.engine.calls.GetNotificationTypesUseCaseInterface
import com.walletconnect.notify.engine.calls.IsRegisteredUseCase
import com.walletconnect.notify.engine.calls.IsRegisteredUseCaseInterface
import com.walletconnect.notify.engine.calls.LegacyRegisterUseCase
import com.walletconnect.notify.engine.calls.LegacyRegisterUseCaseInterface
import com.walletconnect.notify.engine.calls.PrepareRegistrationUseCase
import com.walletconnect.notify.engine.calls.PrepareRegistrationUseCaseInterface
import com.walletconnect.notify.engine.calls.RegisterUseCase
import com.walletconnect.notify.engine.calls.RegisterUseCaseInterface
import com.walletconnect.notify.engine.calls.SubscribeToDappUseCase
import com.walletconnect.notify.engine.calls.SubscribeToDappUseCaseInterface
import com.walletconnect.notify.engine.calls.UnregisterUseCase
import com.walletconnect.notify.engine.calls.UnregisterUseCaseInterface
import com.walletconnect.notify.engine.calls.UpdateSubscriptionRequestUseCase
import com.walletconnect.notify.engine.calls.UpdateSubscriptionRequestUseCaseInterface
import org.koin.core.qualifier.named
import org.koin.dsl.module

@JvmSynthetic
internal fun callModule() = module {

    single<SubscribeToDappUseCaseInterface> {
        SubscribeToDappUseCase(
            jsonRpcInteractor = get(),
            crypto = get(),
            extractMetadataFromConfigUseCase = get(),
            metadataStorageRepository = get(),
            fetchDidJwtInteractor = get(),
            extractPublicKeysFromDidJson = get(),
            logger = get()
        )
    }

    single<UpdateSubscriptionRequestUseCaseInterface> {
        UpdateSubscriptionRequestUseCase(
            jsonRpcInteractor = get(),
            subscriptionRepository = get(),
            metadataStorageRepository = get(),
            fetchDidJwtInteractor = get()
        )
    }

    single<DeleteSubscriptionUseCaseInterface> {
        DeleteSubscriptionUseCase(
            jsonRpcInteractor = get(),
            metadataStorageRepository = get(),
            subscriptionRepository = get(),
            messagesRepository = get(),
            fetchDidJwtInteractor = get()
        )
    }

    single<DeleteNotificationUseCaseInterface> {
        DeleteNotificationUseCase(
            messagesRepository = get()
        )
    }

    single<DecryptMessageUseCaseInterface>(named(AndroidCommonDITags.DECRYPT_NOTIFY_MESSAGE)) {
        val useCase = DecryptNotifyMessageUseCase(
            codec = get(),
            serializer = get(),
            jsonRpcHistory = get(),
            messagesRepository = get()
        )

        get<MutableMap<String, DecryptMessageUseCaseInterface>>(named(AndroidCommonDITags.DECRYPT_USE_CASES))[Tags.NOTIFY_MESSAGE.id.toString()] = useCase
        useCase
    }

    single<LegacyRegisterUseCaseInterface> {
        LegacyRegisterUseCase(
            registerIdentityUseCase = get(),
            registeredAccountsRepository = get(),
            watchSubscriptionsUseCase = get()
        )
    }

    single<RegisterUseCaseInterface> {
        RegisterUseCase(
            registeredAccountsRepository = get(),
            identitiesInteractor = get(),
            watchSubscriptionsUseCase = get(),
            keyManagementRepository = get(),
            projectId = get()
        )
    }

    single<IsRegisteredUseCaseInterface> {
        IsRegisteredUseCase(
            registeredAccountsRepository = get(),
            identitiesInteractor = get(),
            identityServerUrl = get(named(AndroidCommonDITags.KEYSERVER_URL))
        )
    }

    single<PrepareRegistrationUseCaseInterface> {
        PrepareRegistrationUseCase(
            identitiesInteractor = get(),
            identityServerUrl = get(named(AndroidCommonDITags.KEYSERVER_URL)),
            keyManagementRepository = get()
        )
    }

    single<UnregisterUseCaseInterface> {
        UnregisterUseCase(
            registeredAccountsRepository = get(),
            stopWatchingSubscriptionsUseCase = get(),
            identitiesInteractor = get(),
            keyserverUrl = get(named(AndroidCommonDITags.KEYSERVER_URL)),
            subscriptionRepository = get(),
            messagesRepository = get(),
            jsonRpcInteractor = get()
        )
    }

    single<GetNotificationTypesUseCaseInterface> {
        GetNotificationTypesUseCase(
            getNotifyConfigUseCase = get()
        )
    }

    single<GetListOfActiveSubscriptionsUseCaseInterface> {
        GetListOfActiveSubscriptionsUseCase(
            subscriptionRepository = get(),
            metadataStorageRepository = get()
        )
    }

    single<GetListOfNotificationsUseCaseInterface> {
        GetListOfNotificationsUseCase(
            messagesRepository = get(),
            metadataStorageRepository = get()
        )
    }
}