@file:JvmSynthetic

package com.walletconnect.notify.common.model

import com.walletconnect.android.internal.common.model.type.EngineEvent

internal sealed class UpdateSubscription : EngineEvent {

    data class Success(val subscription: Subscription.Active) : UpdateSubscription()

    data class Error(val throwable: Throwable) : UpdateSubscription()

    object Processing : UpdateSubscription()
}