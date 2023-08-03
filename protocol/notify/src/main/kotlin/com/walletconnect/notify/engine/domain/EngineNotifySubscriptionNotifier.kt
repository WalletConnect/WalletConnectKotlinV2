@file:JvmSynthetic

package com.walletconnect.notify.engine.domain

import com.walletconnect.notify.common.model.Subscription
import kotlinx.coroutines.flow.MutableStateFlow

internal class EngineNotifySubscriptionNotifier {
    val newlyRespondedRequestedSubscriptionId = MutableStateFlow<Pair<Long, Subscription.Active>?>(null)
}