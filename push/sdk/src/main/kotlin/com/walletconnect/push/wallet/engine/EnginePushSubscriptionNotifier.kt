package com.walletconnect.push.wallet.engine

import com.walletconnect.push.common.model.EngineDO
import kotlinx.coroutines.flow.MutableStateFlow

internal class EnginePushSubscriptionNotifier {
    val newlyCreatedPushSubscription = MutableStateFlow<EngineDO.PushSubscription?>(null)
}