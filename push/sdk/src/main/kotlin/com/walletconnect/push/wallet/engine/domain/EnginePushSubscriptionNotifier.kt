@file:JvmSynthetic

package com.walletconnect.push.wallet.engine.domain

import com.walletconnect.push.common.model.EngineDO
import kotlinx.coroutines.flow.MutableStateFlow

internal class EnginePushSubscriptionNotifier {
    val newlyRespondedRequestedSubscriptionId = MutableStateFlow<Pair<Long, EngineDO.Subscription.Active>?>(null)
}