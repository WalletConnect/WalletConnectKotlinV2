package com.walletconnect.chat.engine.sync.updates

import com.walletconnect.android.sync.common.model.Events


internal class OnSyncUpdateUseCase(
    private val onChatInviteUpdateUseCase: OnChatInviteUpdateUseCase,
    private val onThreadsUpdateUseCase: OnThreadsUpdateUseCase,
    private val onInviteKeysUpdateUseCase: OnInviteKeysUpdateUseCase,
) {
    operator fun invoke(event: Events.OnSyncUpdate) {
        when (event.store.value) {
            CHAT_SENT_INVITES -> onChatInviteUpdateUseCase(event)
            CHAT_THREADS -> onThreadsUpdateUseCase(event)
            CHAT_INVITE_KEYS -> onInviteKeysUpdateUseCase(event)
        }
    }

    companion object {
        const val CHAT_SENT_INVITES = "chatSentInvites"
        const val CHAT_THREADS = "chatThreads" // imo needs to sync threads sym keys not threads itself
        const val CHAT_INVITE_KEYS = "chatInviteKeys" // imo needs to sync threads sym keys not threads itself
    }
}

