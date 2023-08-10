package com.walletconnect.push.engine.sync.use_case.requests

import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.sync.client.Sync
import com.walletconnect.android.sync.client.SyncInterface
import com.walletconnect.android.sync.common.model.Store
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.util.Logger
import com.walletconnect.push.engine.sync.NotifySyncStores

internal class DeleteSubscriptionToPushSubscriptionStoreUseCase(
    private val logger: Logger,
    private val syncClient: SyncInterface,
) {

    suspend operator fun invoke(accountId: AccountId, pushTopic: Topic, onSuccess: (Boolean) -> Unit, onError: (Throwable) -> Unit) {
        syncClient.delete(
            Sync.Params.Delete(accountId, Store(NotifySyncStores.NOTIFY_SUBSCRIPTION.value), pushTopic.value),
            onSuccess = { didUpdate -> onSuccess(didUpdate) },
            onError = { error -> onError(error.throwable).also { logger.error(error.throwable) } }
        )
    }
}

