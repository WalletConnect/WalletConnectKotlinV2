@file:JvmSynthetic

package com.walletconnect.notify.common.model

import com.walletconnect.android.internal.common.model.type.EngineEvent

internal sealed class CreateSubscription : EngineEvent {

    data class Success(val subscription: Subscription.Active) : CreateSubscription()

    data class Error(val throwable: Throwable) : CreateSubscription()

    object Processing : CreateSubscription()
}