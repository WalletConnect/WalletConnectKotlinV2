package com.walletconnect.chat.engine.sync.use_case.requests

import com.squareup.moshi.Moshi
import com.walletconnect.android.sync.client.Sync
import com.walletconnect.android.sync.client.SyncInterface
import com.walletconnect.android.sync.common.model.Store
import com.walletconnect.chat.common.model.Invite
import com.walletconnect.chat.common.model.InviteStatus
import com.walletconnect.chat.engine.sync.ChatSyncStores
import com.walletconnect.chat.engine.sync.model.SyncedReceivedInviteStatus
import com.walletconnect.chat.engine.sync.model.toSync
import com.walletconnect.foundation.util.Logger

internal class SetReceivedInviteStatusToChatSentInvitesStoreUseCase(
    private val logger: Logger,
    private val syncClient: SyncInterface,
    _moshi: Moshi.Builder,
) {
    private val moshi = _moshi.build()

    operator fun invoke(receivedInvite: Invite.Received, onSuccess: (Boolean) -> Unit, onError: (Throwable) -> Unit) {
        logger.log(receivedInvite.toString())

        if (receivedInvite.status != InviteStatus.REJECTED) {
            logger.log("${ChatSyncStores.CHAT_RECEIVED_INVITE_STATUSES.value} expects only updates about `rejected` status")
            return onSuccess(false)
        }

        val syncedReceivedInviteStatus = receivedInvite.toSync()
        val payload = moshi.adapter(SyncedReceivedInviteStatus::class.java).toJson(syncedReceivedInviteStatus)
        logger.log("SyncedReceivedInviteStatus: $payload")

        syncClient.set(
            Sync.Params.Set(receivedInvite.inviteeAccount, Store(ChatSyncStores.CHAT_RECEIVED_INVITE_STATUSES.value), receivedInvite.id.toString(), payload),
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

