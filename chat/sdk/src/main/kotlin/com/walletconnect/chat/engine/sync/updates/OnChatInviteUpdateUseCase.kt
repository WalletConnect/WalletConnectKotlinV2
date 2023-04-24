package com.walletconnect.chat.engine.sync.updates

import com.walletconnect.android.sync.common.model.Events
import com.walletconnect.android.sync.common.model.SyncUpdate
import com.walletconnect.chat.engine.use_case.calls.AcceptInviteUseCase

internal class OnChatInviteUpdateUseCase(
    private val acceptInviteUseCase: AcceptInviteUseCase,
) {
    operator fun invoke(event: Events.OnSyncUpdate) {
        // When the chat invite update come it means someone replied to an invite from other client
        when (val update = event.update) {
            is SyncUpdate.SyncSet -> {
                acceptInviteUseCase.accept(update.value.toLong(), {}, {}) //todo
            }
            is SyncUpdate.SyncDelete -> TODO()
        }
    }
}