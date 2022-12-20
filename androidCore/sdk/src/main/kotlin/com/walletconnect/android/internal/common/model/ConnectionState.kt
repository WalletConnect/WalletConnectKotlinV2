package com.walletconnect.android.internal.common.model

import com.walletconnect.android.internal.common.model.type.EngineEvent

data class ConnectionState(val isAvailable: Boolean, val throwable: Throwable? = null) : EngineEvent
