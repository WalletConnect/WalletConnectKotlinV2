package com.walletconnect.chat.engine.sync.use_case.requests

import com.squareup.moshi.Moshi
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.sync.client.Sync
import com.walletconnect.android.sync.client.SyncInterface
import com.walletconnect.android.sync.common.model.Store
import com.walletconnect.chat.engine.sync.ChatSyncStores
import com.walletconnect.chat.engine.sync.model.SyncedReceivedInviteRejectedStatus
import com.walletconnect.chat.engine.sync.model.toSync
import com.walletconnect.foundation.util.Logger

internal class SetReceivedInviteRejectedStatusToChatSentInvitesStoreUseCase(
    private val logger: Logger,
    private val syncClient: SyncInterface,
    _moshi: Moshi.Builder,
) {
    private val moshi = _moshi.build()

    operator fun invoke(account: AccountId, inviteId: Long, onSuccess: (Boolean) -> Unit, onError: (Throwable) -> Unit) {


        val syncedReceivedInviteRejectedStatus = inviteId.toSync()
        val payload = moshi.adapter(SyncedReceivedInviteRejectedStatus::class.java).toJson(syncedReceivedInviteRejectedStatus)
        logger.log("SyncedReceivedInviteStatus: $payload")

        syncClient.set(
            Sync.Params.Set(account, Store(ChatSyncStores.CHAT_RECEIVED_INVITE_STATUSES.value), inviteId.toString(), payload),
            onSuccess = { didUpdate ->
                logger.log("Did update on ${ChatSyncStores.CHAT_RECEIVED_INVITE_STATUSES.value} happen: $didUpdate")
                onSuccess(didUpdate)
            },
            onError = { error ->
                logger.error(error.throwable)
                onError(error.throwable)
            }
        )
    }
}

