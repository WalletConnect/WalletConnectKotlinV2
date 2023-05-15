package com.walletconnect.chat.engine.sync.use_case.events

import android.database.sqlite.SQLiteConstraintException
import com.squareup.moshi.Moshi
import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.sync.common.model.Events
import com.walletconnect.android.sync.common.model.SyncUpdate
import com.walletconnect.chat.common.model.Invite
import com.walletconnect.chat.engine.sync.model.SyncedSentInvite
import com.walletconnect.chat.engine.sync.model.toCommon
import com.walletconnect.chat.storage.InvitesStorageRepository
import com.walletconnect.foundation.util.Logger

internal class OnChatSentInviteUpdateEventUseCase(
    private val logger: Logger,
    private val invitesRepository: InvitesStorageRepository,
    private val keyManagementRepository: KeyManagementRepository,
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    _moshi: Moshi.Builder,
) {
    private val moshi = _moshi.build()

    suspend operator fun invoke(event: Events.OnSyncUpdate) {

        // When the chat sent invite update comes it means someone sent an invite or updated the status from other client
        if (event.update is SyncUpdate.SyncSet) {
            logger.log(event.toString())
            val update = (event.update as SyncUpdate.SyncSet)
            val syncedSentInvite: SyncedSentInvite = moshi.adapter(SyncedSentInvite::class.java).fromJson(update.value) ?: return logger.error(event.toString())
            val sentInvite = syncedSentInvite.toCommon()
            val storedSentInvite: Invite.Sent? = runCatching { invitesRepository.getSentInviteByInviteId(sentInvite.id) }.getOrNull()

            if (storedSentInvite == null) {
                runCatching { invitesRepository.insertInvite(sentInvite) }.fold(onSuccess = {
                    keyManagementRepository.setKey(sentInvite.symmetricKey, sentInvite.acceptTopic.value)
                    keyManagementRepository.setKeyPair(sentInvite.inviterPublicKey, sentInvite.inviterPrivateKey!!)
                    jsonRpcInteractor.subscribe(sentInvite.acceptTopic) { error -> logger.error(error) }
                }, onFailure = { error -> if (error !is SQLiteConstraintException) logger.error(error) })
            } else {
                invitesRepository.updateStatusByInviteId(sentInvite.id, sentInvite.status)
            }
        }
    }
}