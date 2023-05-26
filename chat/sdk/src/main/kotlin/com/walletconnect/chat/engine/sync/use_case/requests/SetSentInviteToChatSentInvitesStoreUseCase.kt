package com.walletconnect.chat.engine.sync.use_case.requests

import com.squareup.moshi.Moshi
import com.walletconnect.android.sync.client.Sync
import com.walletconnect.android.sync.client.SyncInterface
import com.walletconnect.android.sync.common.model.Store
import com.walletconnect.chat.common.model.Invite
import com.walletconnect.chat.engine.sync.ChatSyncStores
import com.walletconnect.chat.engine.sync.model.SyncedSentInvite
import com.walletconnect.chat.engine.sync.model.toSync
import com.walletconnect.foundation.util.Logger

internal class SetSentInviteToChatSentInvitesStoreUseCase(
    private val logger: Logger,
    private val syncClient: SyncInterface,
    _moshi: Moshi.Builder,
) {
    private val moshi = _moshi.build()

    operator fun invoke(sentInvite: Invite.Sent, onSuccess: (Boolean) -> Unit, onError: (Throwable) -> Unit) {
        val syncedSentInvite = sentInvite.toSync()
        val payload = moshi.adapter(SyncedSentInvite::class.java).toJson(syncedSentInvite)

        syncClient.set(
            Sync.Params.Set(sentInvite.inviterAccount, Store(ChatSyncStores.CHAT_SENT_INVITES.value), sentInvite.acceptTopic.value, payload),
            onSuccess = { didUpdate -> onSuccess(didUpdate) },
            onError = { error -> onError(error.throwable).also { logger.error(error.throwable) } }
        )
    }
}

