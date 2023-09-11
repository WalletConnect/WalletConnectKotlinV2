package com.walletconnect.notify.common.model

import com.walletconnect.android.internal.common.model.type.EngineEvent

internal data class SubscriptionChanged(
    val subscriptions: List<Subscription.Active>,
) : EngineEvent
