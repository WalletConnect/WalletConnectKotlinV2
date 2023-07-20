package com.walletconnect.chat.engine.sync.use_case.events

import android.database.sqlite.SQLiteConstraintException
import com.squareup.moshi.Moshi
import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.sync.common.model.Events
import com.walletconnect.android.sync.common.model.SyncUpdate
import com.walletconnect.chat.common.model.InviteStatus
import com.walletconnect.chat.engine.sync.model.SyncedThread
import com.walletconnect.chat.engine.sync.model.toCommon
import com.walletconnect.chat.storage.InvitesStorageRepository
import com.walletconnect.chat.storage.ThreadsStorageRepository
import com.walletconnect.foundation.util.Logger

internal class OnThreadsUpdateEventUseCase(
    private val logger: Logger,
    private val threadsRepository: ThreadsStorageRepository,
    private val invitesRepository: InvitesStorageRepository,
    private val keyManagementRepository: KeyManagementRepository,
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    _moshi: Moshi.Builder,
) {
    private val moshi = _moshi.build()

    suspend operator fun invoke(event: Events.OnSyncUpdate) {

        // When the chat thread update comes it means someone either accepted invite or someone received on invite accepted response
        if (event.update is SyncUpdate.SyncSet) {
            val update = (event.update as SyncUpdate.SyncSet)
            val syncedThread: SyncedThread = moshi.adapter(SyncedThread::class.java).fromJson(update.value) ?: return logger.error(event.toString())
            val (thread, symmetricKey) = syncedThread.toCommon()
            val storedThread = runCatching { threadsRepository.getThreadByTopic(thread.topic.value) }.getOrNull()

            if (storedThread == null) {
                runCatching { threadsRepository.insertThread(thread.topic.value, thread.selfAccount.value, thread.peerAccount.value) }.fold(onSuccess = {
                    keyManagementRepository.setKey(symmetricKey, thread.topic.value)
                    jsonRpcInteractor.subscribe(thread.topic) { error -> logger.error(error) }

                    // note: This has non-hostile race with status update on sent invites from OnChatSentInviteUpdateEventUseCase
                    invitesRepository.updateStatusByAccounts(thread.selfAccount.value, thread.peerAccount.value, InviteStatus.APPROVED)
                }, onFailure = { error -> if (error !is SQLiteConstraintException) logger.error(error) })
            } else {
                logger.error(Throwable("Thread is already stored"))
            }
        }
    }
}