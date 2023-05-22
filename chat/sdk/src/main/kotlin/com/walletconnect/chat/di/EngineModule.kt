@file:JvmSynthetic

package com.walletconnect.chat.di

import com.walletconnect.android.internal.common.di.AndroidCommonDITags
import com.walletconnect.chat.engine.domain.ChatEngine
import com.walletconnect.chat.engine.use_case.SubscribeToChatTopicsUseCase
import com.walletconnect.chat.engine.use_case.calls.*
import com.walletconnect.chat.engine.use_case.requests.OnInviteRequestUseCase
import com.walletconnect.chat.engine.use_case.requests.OnLeaveRequestUseCase
import com.walletconnect.chat.engine.use_case.requests.OnMessageRequestUseCase
import com.walletconnect.chat.engine.use_case.responses.OnInviteResponseUseCase
import com.walletconnect.chat.engine.use_case.responses.OnLeaveResponseUseCase
import com.walletconnect.chat.engine.use_case.responses.OnMessageResponseUseCase
import com.walletconnect.chat.json_rpc.GetPendingJsonRpcHistoryEntryByIdUseCase
import org.koin.core.qualifier.named
import org.koin.dsl.module

@JvmSynthetic
internal fun engineModule() = module {
    single { GetPendingJsonRpcHistoryEntryByIdUseCase(get(), get()) }

    single {
        AcceptInviteUseCase(
            keyserverUrl = get(named(AndroidCommonDITags.KEYSERVER_URL)),
            getPendingJsonRpcHistoryEntryByIdUseCase = get(),
            logger = get(),
            invitesRepository = get(),
            keyManagementRepository = get(),
            identitiesInteractor = get(),
            jsonRpcInteractor = get(),
            threadsRepository = get(),
            setThreadWithSymmetricKeyToChatThreadsStoreUseCase = get(),
        )
    }

    single {
        RejectInviteUseCase(
            getPendingJsonRpcHistoryEntryByIdUseCase = get(),
            logger = get(),
            invitesRepository = get(),
            keyManagementRepository = get(),
            jsonRpcInteractor = get(),
        )
    }

    single {
        RegisterIdentityUseCase(
            keyserverUrl = get(named(AndroidCommonDITags.KEYSERVER_URL)),
            identitiesInteractor = get(),
            accountsRepository = get(),
            goPublicUseCase = get(),
            setupSyncInChatUseCase = get()
        )
    }

    single {
        UnregisterIdentityUseCase(
            keyserverUrl = get(named(AndroidCommonDITags.KEYSERVER_URL)),
            identitiesInteractor = get(),
            accountsRepository = get(),
            keyManagementRepository = get(),
            jsonRpcInteractor = get(),
        )
    }

    single {
        GoPublicUseCase(
            keyserverUrl = get(named(AndroidCommonDITags.KEYSERVER_URL)),
            identitiesInteractor = get(),
            accountsRepository = get(),
            keyManagementRepository = get(),
            jsonRpcInteractor = get(),
            registerInviteUseCase = get(),
            setInviteKeyToChatInviteKeyStoreUseCase = get()
        )
    }

    single {
        GoPrivateUseCase(
            keyserverUrl = get(named(AndroidCommonDITags.KEYSERVER_URL)),
            identitiesInteractor = get(),
            accountsRepository = get(),
            keyManagementRepository = get(),
            jsonRpcInteractor = get(),
            unregisterInviteUseCase = get(),
            deleteInviteKeyFromChatInviteKeyStoreUseCase = get()
        )
    }

    single {
        SendInviteUseCase(
            keyserverUrl = get(named(AndroidCommonDITags.KEYSERVER_URL)),
            logger = get(),
            invitesRepository = get(),
            keyManagementRepository = get(),
            identitiesInteractor = get(),
            jsonRpcInteractor = get(),
            contactRepository = get(),
            setSentInviteToChatSentInvitesStoreUseCase = get(),
        )
    }

    single {
        SendMessageUseCase(
            keyserverUrl = get(named(AndroidCommonDITags.KEYSERVER_URL)),
            logger = get(),
            identitiesInteractor = get(),
            jsonRpcInteractor = get(),
            threadsRepository = get(),
            messageRepository = get()
        )
    }

    single { LeaveThreadUseCase(logger = get(), jsonRpcInteractor = get(), threadsRepository = get(), messageRepository = get()) }
    single { ResolveAccountUseCase(resolveInviteUseCase = get()) }
    single { GetThreadsUseCase(get()) }
    single { GetMessagesUseCase(get()) }
    single { GetSentInvitesUseCase(get()) }
    single { GetReceivedInvitesUseCase(get()) }
    single { SendPingUseCase(logger = get(), jsonRpcInteractor = get()) }
    single { OnMessageResponseUseCase(logger = get()) }
    single { OnLeaveResponseUseCase(logger = get()) }
    single {
        OnInviteRequestUseCase(
            logger = get(), identitiesInteractor = get(), accountsRepository = get(), invitesRepository = get(), keyManagementRepository = get(),
            setReceivedInviteStatusToChatSentInvitesStoreUseCase = get()
        )
    }
    single { OnMessageRequestUseCase(logger = get(), identitiesInteractor = get(), messageRepository = get(), keyserverUrl = get(named(AndroidCommonDITags.KEYSERVER_URL)), jsonRpcInteractor = get()) }
    single { OnLeaveRequestUseCase(messageRepository = get(), jsonRpcInteractor = get(), threadsRepository = get()) }
    single {
        OnInviteResponseUseCase(
            logger = get(), invitesRepository = get(), keyManagementRepository = get(), identitiesInteractor = get(), threadsRepository = get(), jsonRpcInteractor = get(),
            setThreadWithSymmetricKeyToChatThreadsStoreUseCase = get(),
            setSentInviteToChatSentInvitesStoreUseCase = get()
        )
    }
    single { SubscribeToChatTopicsUseCase(logger = get(), invitesRepository = get(), accountsRepository = get(), threadsRepository = get(), jsonRpcInteractor = get()) }

    single {
        ChatEngine(
            jsonRpcInteractor = get(),
            pairingHandler = get(),
            syncClient = get(),
            onSyncUpdateEventUseCase = get(),
            acceptInviteUseCase = get(),
            rejectInviteUseCase = get(),
            goPublicUseCase = get(),
            goPrivateUseCase = get(),
            registerIdentityUseCase = get(),
            onInviteRequestUseCase = get(),
            onMessageRequestUseCase = get(),
            sendInviteUseCase = get(),
            sendMessageUseCase = get(),
            unregisterIdentityUseCase = get(),
            resolveAccountUseCase = get(),
            leaveThreadUseCase = get(),
            sendPingUseCase = get(),
            onInviteResponseUseCase = get(),
            getThreadsUseCase = get(),
            getMessagesUseCase = get(),
            getReceivedInvitesUseCase = get(),
            getSentInvitesUseCase = get(),
            onLeaveRequestUseCase = get(),
            onLeaveResponseUseCase = get(),
            onMessageResponseUseCase = get(),
            subscribeToChatTopicsUseCase = get()
        )
    }
}