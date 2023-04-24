@file:JvmSynthetic

package com.walletconnect.chat.di

import com.walletconnect.android.internal.common.di.AndroidCommonDITags
import com.walletconnect.chat.engine.domain.ChatEngine
import com.walletconnect.chat.engine.use_case.calls.AcceptInviteUseCase
import com.walletconnect.chat.json_rpc.GetPendingJsonRpcHistoryEntryByIdUseCase
import org.koin.core.qualifier.named
import org.koin.dsl.module

@JvmSynthetic
internal fun engineModule() = module {
    single { GetPendingJsonRpcHistoryEntryByIdUseCase(get(), get()) }

    single { AcceptInviteUseCase(get(), get(), get(), get(), get(), get(), get(), get()) }

    single {
        ChatEngine(
            keyserverUrl = get(named(AndroidCommonDITags.KEYSERVER_URL)),
            getPendingJsonRpcHistoryEntryByIdUseCase = get(),
            identitiesInteractor = get(),
            registerInviteUseCase = get(),
            unregisterInviteUseCase = get(),
            resolveInviteUseCase = get(),
            keyManagementRepository = get(),
            jsonRpcInteractor = get(),
            contactRepository = get(),
            pairingHandler = get(),
            threadsRepository = get(),
            invitesRepository = get(),
            messageRepository = get(),
            accountsRepository = get(),
            logger = get(named(AndroidCommonDITags.LOGGER)),
            syncInterface = get(),
            onSyncUpdateUseCase = get(),
            acceptInviteUseCase = get()
        )
    }
}