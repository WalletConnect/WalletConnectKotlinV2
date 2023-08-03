package com.walletconnect.web3.inbox.sync.event

import com.walletconnect.android.sync.common.model.Events
import com.walletconnect.chat.engine.sync.ChatSyncStores
import com.walletconnect.push.engine.sync.PushSyncStores
import com.walletconnect.web3.inbox.chat.event.OnSyncUpdateChatEventUseCase
import com.walletconnect.web3.inbox.push.event.OnSyncUpdateNotifyEventUseCase

internal class OnSyncUpdateEventUseCase(
    val onSyncUpdateChatEventUseCase: OnSyncUpdateChatEventUseCase,
    val onSyncUpdateNotifyEventUseCase: OnSyncUpdateNotifyEventUseCase,
) {
    operator fun invoke(event: Events.OnSyncUpdate) {
        when {
            ChatSyncStores.values().firstOrNull { it.value == event.store.value } != null -> onSyncUpdateChatEventUseCase(event)
            PushSyncStores.values().firstOrNull { it.value == event.store.value } != null -> onSyncUpdateNotifyEventUseCase(event)
        }
    }
}

