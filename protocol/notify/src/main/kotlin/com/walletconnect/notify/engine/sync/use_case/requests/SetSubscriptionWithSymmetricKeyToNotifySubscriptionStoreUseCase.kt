@file:JvmSynthetic

package com.walletconnect.notify.engine.sync.use_case.requests

import com.squareup.moshi.Moshi
import com.walletconnect.android.internal.common.model.SymmetricKey
import com.walletconnect.android.sync.client.Sync
import com.walletconnect.android.sync.client.SyncInterface
import com.walletconnect.android.sync.common.model.Store
import com.walletconnect.foundation.util.Logger
import com.walletconnect.notify.common.model.EngineDO
import com.walletconnect.notify.engine.sync.NotifySyncStores
import com.walletconnect.notify.engine.sync.model.SyncedSubscription
import com.walletconnect.notify.engine.sync.model.toSync

internal class SetSubscriptionWithSymmetricKeyToNotifySubscriptionStoreUseCase(
    private val logger: Logger,
    private val syncClient: SyncInterface,
    @Suppress("LocalVariableName") _moshi: Moshi.Builder,
) {
    private val moshi = _moshi.build()

    operator fun invoke(subscription: EngineDO.Subscription.Active, symmetricKey: SymmetricKey, onSuccess: (Boolean) -> Unit, onError: (Throwable) -> Unit) {
        val syncedSubscription = subscription.toSync(symmetricKey)
        val payload = moshi.adapter(SyncedSubscription::class.java).toJson(syncedSubscription)

        syncClient.set(
            Sync.Params.Set(subscription.account, Store(NotifySyncStores.NOTIFY_SUBSCRIPTION.value), subscription.notifyTopic.value, payload),
            onSuccess = { didUpdate -> onSuccess(didUpdate) },
            onError = { error -> onError(error.throwable).also { logger.error(error.throwable) } }
        )
    }
}

