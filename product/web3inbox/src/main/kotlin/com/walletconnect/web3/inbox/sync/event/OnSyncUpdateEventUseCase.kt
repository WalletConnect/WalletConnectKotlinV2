package com.walletconnect.web3.inbox.sync.event

import com.walletconnect.android.sync.common.model.Events
import com.walletconnect.chat.engine.sync.ChatSyncStores
import com.walletconnect.web3.inbox.chat.event.OnSyncUpdateChatEventUseCase

internal class OnSyncUpdateEventUseCase(
    val onSyncUpdateChatEventUseCase: OnSyncUpdateChatEventUseCase,
) {
    operator fun invoke(event: Events.OnSyncUpdate) {
        when {
            ChatSyncStores.values().firstOrNull { it.value == event.store.value } != null -> onSyncUpdateChatEventUseCase(event)
        }
    }
}

