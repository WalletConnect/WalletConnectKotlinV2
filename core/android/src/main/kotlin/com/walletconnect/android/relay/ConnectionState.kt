@file:JvmSynthetic

package com.walletconnect.android.relay

sealed class WSSConnectionState {
	data object Connected : WSSConnectionState()

	sealed class Disconnected : WSSConnectionState() {
		data class ConnectionFailed(val throwable: Throwable) : Disconnected()
		data class ConnectionClosed(val message: String? = null) : Disconnected()
	}
}