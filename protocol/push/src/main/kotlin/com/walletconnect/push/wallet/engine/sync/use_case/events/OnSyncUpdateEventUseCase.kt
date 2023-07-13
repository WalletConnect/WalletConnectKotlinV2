package com.walletconnect.push.wallet.engine.sync.use_case.events

import com.walletconnect.android.sync.common.model.Events
import com.walletconnect.push.wallet.engine.sync.PushSyncStores


internal class OnSyncUpdateEventUseCase(
    val onSubscriptionUpdateEventUseCase: OnSubscriptionUpdateEventUseCase,
) {
    suspend operator fun invoke(event: Events.OnSyncUpdate) {
        when (event.store.value) {
            PushSyncStores.PUSH_SUBSCRIPTION.value -> onSubscriptionUpdateEventUseCase(event)
        }
    }
}

