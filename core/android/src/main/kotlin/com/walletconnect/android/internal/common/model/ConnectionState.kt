package com.walletconnect.android.internal.common.model

import com.walletconnect.android.internal.common.model.type.EngineEvent

data class ConnectionState(val isAvailable: Boolean, val reason: Reason? = null) : EngineEvent {
	sealed class Reason {
		data class ConnectionClosed(val message: String) : Reason()
		data class ConnectionFailed(val throwable: Throwable) : Reason()
	}
}
