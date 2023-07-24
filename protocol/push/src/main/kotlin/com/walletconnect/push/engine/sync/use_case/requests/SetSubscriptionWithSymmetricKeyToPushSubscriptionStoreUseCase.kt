package com.walletconnect.push.engine.sync.use_case.requests

import com.squareup.moshi.Moshi
import com.walletconnect.android.internal.common.model.SymmetricKey
import com.walletconnect.android.sync.client.Sync
import com.walletconnect.android.sync.client.SyncInterface
import com.walletconnect.android.sync.common.model.Store
import com.walletconnect.foundation.util.Logger
import com.walletconnect.push.common.model.EngineDO
import com.walletconnect.push.engine.sync.PushSyncStores
import com.walletconnect.push.engine.sync.model.SyncedSubscription
import com.walletconnect.push.engine.sync.model.toSync

internal class SetSubscriptionWithSymmetricKeyToPushSubscriptionStoreUseCase(
    private val logger: Logger,
    private val syncClient: SyncInterface,
    _moshi: Moshi.Builder,
) {
    private val moshi = _moshi.build()

    suspend operator fun invoke(subscription: EngineDO.Subscription.Active, symmetricKey: SymmetricKey, onSuccess: (Boolean) -> Unit, onError: (Throwable) -> Unit) {
        val syncedSubscription = subscription.toSync(symmetricKey)
        val payload = moshi.adapter(SyncedSubscription::class.java).toJson(syncedSubscription)

        syncClient.set(
            Sync.Params.Set(subscription.account, Store(PushSyncStores.PUSH_SUBSCRIPTION.value), subscription.pushTopic.value, payload),
            onSuccess = { didUpdate -> onSuccess(didUpdate) },
            onError = { error -> onError(error.throwable).also { logger.error(error.throwable) } }
        )
    }
}

