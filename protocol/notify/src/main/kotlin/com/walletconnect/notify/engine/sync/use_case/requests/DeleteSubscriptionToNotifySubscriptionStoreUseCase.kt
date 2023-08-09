@file:JvmSynthetic

package com.walletconnect.notify.engine.sync.use_case.requests

import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.sync.client.Sync
import com.walletconnect.android.sync.client.SyncInterface
import com.walletconnect.android.sync.common.model.Store
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.util.Logger
import com.walletconnect.notify.engine.sync.NotifySyncStores

internal class DeleteSubscriptionToNotifySubscriptionStoreUseCase(
    private val logger: Logger,
    private val syncClient: SyncInterface,
) {

    operator fun invoke(accountId: AccountId, notifyTopic: Topic, onSuccess: (Boolean) -> Unit, onError: (Throwable) -> Unit) {
        syncClient.delete(
            Sync.Params.Delete(accountId, Store(NotifySyncStores.NOTIFY_SUBSCRIPTION.value), notifyTopic.value),
            onSuccess = { didUpdate -> onSuccess(didUpdate) },
            onError = { error -> onError(error.throwable).also { logger.error(error.throwable) } }
        )
    }
}

