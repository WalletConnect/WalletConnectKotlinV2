package com.walletconnect.android.impl.common.model

import com.walletconnect.android.impl.common.model.type.EngineEvent

data class ConnectionState(val isAvailable: Boolean, val throwable: Throwable? = null) : EngineEvent
