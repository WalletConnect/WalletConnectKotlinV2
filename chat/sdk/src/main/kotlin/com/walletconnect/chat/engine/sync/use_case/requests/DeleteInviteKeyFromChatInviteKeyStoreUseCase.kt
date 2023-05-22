package com.walletconnect.chat.engine.sync.use_case.requests

import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.sync.client.Sync
import com.walletconnect.android.sync.client.SyncInterface
import com.walletconnect.android.sync.common.model.Store
import com.walletconnect.chat.engine.sync.ChatSyncStores
import com.walletconnect.foundation.util.Logger

internal class DeleteInviteKeyFromChatInviteKeyStoreUseCase(
    private val logger: Logger,
    private val syncClient: SyncInterface,
) {
    operator fun invoke(account: AccountId) {
        syncClient.delete(
            Sync.Params.Delete(account, Store(ChatSyncStores.CHAT_INVITE_KEYS.value), account.value),
            onSuccess = { logger.log("Did update on ${ChatSyncStores.CHAT_INVITE_KEYS.value} happen: $it") },
            onError = { logger.error(it.throwable) }
        )
    }
}

