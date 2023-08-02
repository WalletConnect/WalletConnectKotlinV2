@file:JvmSynthetic

package com.walletconnect.notify.engine.domain

import com.walletconnect.notify.common.model.EngineDO
import kotlinx.coroutines.flow.MutableStateFlow

internal class EngineNotifySubscriptionNotifier {
    val newlyRespondedRequestedSubscriptionId = MutableStateFlow<Pair<Long, EngineDO.Subscription.Active>?>(null)
}