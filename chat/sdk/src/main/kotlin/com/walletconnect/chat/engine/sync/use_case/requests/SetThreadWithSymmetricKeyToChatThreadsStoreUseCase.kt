package com.walletconnect.chat.engine.sync.use_case.requests

import com.squareup.moshi.Moshi
import com.walletconnect.android.internal.common.model.SymmetricKey
import com.walletconnect.android.sync.client.Sync
import com.walletconnect.android.sync.client.SyncInterface
import com.walletconnect.android.sync.common.model.Store
import com.walletconnect.chat.common.model.Thread
import com.walletconnect.chat.engine.sync.ChatSyncStores
import com.walletconnect.chat.engine.sync.model.SyncedThread
import com.walletconnect.chat.engine.sync.model.toSync
import com.walletconnect.foundation.util.Logger

internal class SetThreadWithSymmetricKeyToChatThreadsStoreUseCase(
    private val logger: Logger,
    private val syncClient: SyncInterface,
    _moshi: Moshi.Builder,
) {

    private val moshi = _moshi.build()

    operator fun invoke(thread: Thread, symmetricKey: SymmetricKey, onSuccess: (Boolean) -> Unit, onError: (Throwable) -> Unit) {
        logger.log(thread.toString())
        val syncedThread = thread.toSync(symmetricKey)
        val payload = moshi.adapter(SyncedThread::class.java).toJson(syncedThread)
        logger.log("SyncedThread: $payload")

        syncClient.set(
            Sync.Params.Set(thread.selfAccount, Store(ChatSyncStores.CHAT_THREADS.value), thread.topic.value, payload),
            onSuccess = { didUpdate ->
                logger.log("Did update on ${ChatSyncStores.CHAT_THREADS.value} happen: $didUpdate")
                onSuccess(didUpdate)
            },
            onError = { error ->
                logger.error(error.throwable)
                onError(error.throwable)
            }
        )
    }
}

