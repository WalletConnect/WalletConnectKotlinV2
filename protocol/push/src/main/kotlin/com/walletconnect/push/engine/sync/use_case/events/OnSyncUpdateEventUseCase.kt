package com.walletconnect.push.engine.sync.use_case.events

import com.walletconnect.android.sync.common.model.Events
import com.walletconnect.push.engine.sync.NotifySyncStores


internal class OnSyncUpdateEventUseCase(
    val onSubscriptionUpdateEventUseCase: OnSubscriptionUpdateEventUseCase,
) {
    suspend operator fun invoke(event: Events.OnSyncUpdate) {
        when (event.store.value) {
            NotifySyncStores.NOTIFY_SUBSCRIPTION.value -> onSubscriptionUpdateEventUseCase(event)
        }
    }
}

