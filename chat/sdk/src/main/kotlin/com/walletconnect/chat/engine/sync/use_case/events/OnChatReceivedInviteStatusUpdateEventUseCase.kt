package com.walletconnect.chat.engine.sync.use_case.events

import com.squareup.moshi.Moshi
import com.walletconnect.android.sync.common.model.Events
import com.walletconnect.android.sync.common.model.SyncUpdate
import com.walletconnect.chat.common.exceptions.ReceivedInviteNotStored
import com.walletconnect.chat.common.model.Invite
import com.walletconnect.chat.engine.sync.model.SyncedReceivedInviteRejectedStatus
import com.walletconnect.chat.engine.sync.model.toCommon
import com.walletconnect.chat.storage.InvitesStorageRepository
import com.walletconnect.foundation.util.Logger

internal class OnChatReceivedInviteStatusUpdateEventUseCase(
    private val logger: Logger,
    private val invitesRepository: InvitesStorageRepository,
    _moshi: Moshi.Builder,
) {
    private val moshi = _moshi.build()

    suspend operator fun invoke(event: Events.OnSyncUpdate) {

        // When the chat sent invite update comes it means someone sent an invite or updated the status from other client
        if (event.update is SyncUpdate.SyncSet) {
            logger.log(event.toString())
            val update = (event.update as SyncUpdate.SyncSet)
            val syncedReceivedInviteRejectedStatus: SyncedReceivedInviteRejectedStatus = moshi.adapter(SyncedReceivedInviteRejectedStatus::class.java).fromJson(update.value) ?: return logger.error(event.toString())
            val (receivedInviteId, receivedInviteStatus) = runCatching { syncedReceivedInviteRejectedStatus.toCommon() }.getOrElse { error -> return logger.error(error) }
            val storedReceivedInvite: Invite.Received? = runCatching { invitesRepository.getReceivedInviteByInviteId(receivedInviteId) }.getOrNull()

            if (storedReceivedInvite == null) {
                logger.error(ReceivedInviteNotStored)
            } else {
                invitesRepository.updateStatusByInviteId(receivedInviteId, receivedInviteStatus)
            }
        }
    }
}