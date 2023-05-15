package com.walletconnect.chat.engine.sync.use_case.events

import com.walletconnect.android.sync.common.model.Events
import com.walletconnect.chat.engine.sync.ChatSyncStores.*


internal class OnSyncUpdateEventUseCase(
    private val onChatSentInviteUpdateEventUseCase: OnChatSentInviteUpdateEventUseCase,
    private val onThreadsUpdateEventUseCase: OnThreadsUpdateEventUseCase,
    private val onInviteKeysUpdateEventUseCase: OnInviteKeysUpdateEventUseCase,
) {
    suspend operator fun invoke(event: Events.OnSyncUpdate) {
        when (event.store.value) {
            CHAT_SENT_INVITES.value -> onChatSentInviteUpdateEventUseCase(event)
            CHAT_THREADS.value -> onThreadsUpdateEventUseCase(event)
            CHAT_INVITE_KEYS.value -> onInviteKeysUpdateEventUseCase(event)
        }
    }
}

