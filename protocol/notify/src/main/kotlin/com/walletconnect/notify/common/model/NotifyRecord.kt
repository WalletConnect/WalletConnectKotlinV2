@file:JvmSynthetic

package com.walletconnect.notify.common.model

import com.walletconnect.android.internal.common.model.type.EngineEvent

internal data class NotifyRecord(
    val id: Long,
    val topic: String,
    val publishedAt: Long,
    val notifyMessage: NotifyMessage,
) : EngineEvent