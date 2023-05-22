@file:JvmSynthetic

package com.walletconnect.chat.di

import com.walletconnect.android.internal.common.di.AndroidCommonDITags
import com.walletconnect.chat.engine.sync.use_case.SetupSyncInChatUseCase
import com.walletconnect.chat.engine.sync.use_case.events.*
import com.walletconnect.chat.engine.sync.use_case.requests.*
import org.koin.core.qualifier.named
import org.koin.dsl.module

@JvmSynthetic
internal fun syncInChatModule() = module {
    single { OnChatSentInviteUpdateEventUseCase(get(), get(), get(), get(), get(named(AndroidCommonDITags.MOSHI))) }
    single { OnChatReceivedInviteStatusUpdateEventUseCase(get(), get(), get(named(AndroidCommonDITags.MOSHI))) }
    single { OnInviteKeysUpdateEventUseCase(get(), get(), get(), get(), get(named(AndroidCommonDITags.MOSHI))) }
    single { OnThreadsUpdateEventUseCase(get(), get(), get(), get(), get(), get(named(AndroidCommonDITags.MOSHI))) }
    single { OnSyncUpdateEventUseCase(get(), get(), get(), get()) }
    single { SetSentInviteToChatSentInvitesStoreUseCase(get(), get(), get(named(AndroidCommonDITags.MOSHI))) }
    single { SetReceivedInviteStatusToChatSentInvitesStoreUseCase(get(), get(), get(named(AndroidCommonDITags.MOSHI))) }
    single { SetThreadWithSymmetricKeyToChatThreadsStoreUseCase(get(), get(), get(named(AndroidCommonDITags.MOSHI))) }
    single { SetInviteKeyToChatInviteKeyStoreUseCase(get(), get(), get(named(AndroidCommonDITags.MOSHI))) }
    single { DeleteInviteKeyFromChatInviteKeyStoreUseCase(get(), get()) }
    single { SetupSyncInChatUseCase(get(), get()) }
}