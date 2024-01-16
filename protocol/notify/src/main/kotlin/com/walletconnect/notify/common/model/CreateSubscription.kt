@file:JvmSynthetic

package com.walletconnect.notify.common.model

import com.walletconnect.android.internal.common.model.type.EngineEvent

internal sealed class CreateSubscription : EngineEvent {
    data class Result(
        val subscription: Subscription.Active,
    ) : CreateSubscription()

    data class Error(
        val requestId: Long,
        val rejectionReason: String,
    ) : CreateSubscription()
}