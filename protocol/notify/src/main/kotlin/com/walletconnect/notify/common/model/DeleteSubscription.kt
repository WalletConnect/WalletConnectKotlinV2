@file:JvmSynthetic

package com.walletconnect.notify.common.model

import com.walletconnect.android.internal.common.model.type.EngineEvent

internal sealed class DeleteSubscription : EngineEvent {

    data class Success(val topic: String) : DeleteSubscription()

    data class Error(val throwable: Throwable) : DeleteSubscription()

    object Processing : DeleteSubscription()
}