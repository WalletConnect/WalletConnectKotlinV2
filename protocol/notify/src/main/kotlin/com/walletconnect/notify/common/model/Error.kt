@file:JvmSynthetic

package com.walletconnect.notify.common.model

import com.walletconnect.android.internal.common.model.type.EngineEvent

internal data class Error(
    val requestId: Long,
    val rejectionReason: String,
) : EngineEvent