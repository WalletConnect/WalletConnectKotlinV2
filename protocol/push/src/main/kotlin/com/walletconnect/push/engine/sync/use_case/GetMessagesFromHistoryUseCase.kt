package com.walletconnect.push.engine.sync.use_case

import com.walletconnect.android.history.HistoryInterface
import com.walletconnect.android.history.network.model.messages.MessagesParams
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.sync.client.Sync
import com.walletconnect.android.sync.client.SyncInterface
import com.walletconnect.foundation.util.Logger
import com.walletconnect.push.engine.sync.PushSyncStores
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

internal class GetMessagesFromHistoryUseCase(
    private val historyInterface: HistoryInterface,
    private val syncClient: SyncInterface,
    private val logger: Logger,
) {

    suspend operator fun invoke(accountId: AccountId, onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        getMessagesForPushStores(accountId, onSuccess, onError)
    }

    /**
     * Gets messages of required Push stores from History and calls [onMessagesFetched] on success
     */
    private suspend fun getMessagesForPushStores(accountId: AccountId, onMessagesFetched: () -> Unit, onError: (Throwable) -> Unit) {
        // Register blocking current thread all stores necessary to sync push state
        val countDownLatch = CountDownLatch(PushSyncStores.values().size)

        // Note: When I tried registering all stores simultaneously I had issues with getting right values, when doing it sequentially it works
        PushSyncStores.values().forEach { store ->
            syncClient.getStoreTopic(Sync.Params.GetStoreTopics(accountId, store.value))?.let { topic ->
                val messagesBatchSize = 200L
                historyInterface.getMessages(
                    MessagesParams(topic.value, null, messagesBatchSize, null),
                    onError = { error -> onError(error.throwable) },
                    onSuccess = {
                        //todo: Support fetching more than [messagesBatchSize]
                        if (it.size >= messagesBatchSize) logger.error("Fetched $messagesBatchSize for ${store.value}") else logger.log("Fetched ${it.size} for ${store.value}")
                        countDownLatch.countDown()
                    }
                )
            }
        }

        if (!countDownLatch.await(5, TimeUnit.SECONDS)) {
            onError(Throwable("Required Push Stores initialization timeout"))
        } else {
            onMessagesFetched()
        }
    }
}