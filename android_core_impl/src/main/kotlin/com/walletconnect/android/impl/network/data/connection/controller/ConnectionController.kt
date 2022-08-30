@file:JvmSynthetic

package com.walletconnect.android.impl.network.data.connection.controller

import com.walletconnect.android.impl.network.data.connection.ConnectionEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal sealed class ConnectionController {

    internal class Manual : ConnectionController() {
        private val _connectionEvent: MutableStateFlow<ConnectionEvent> = MutableStateFlow(
            ConnectionEvent.DISCONNECT)
        val connectionEventFlow: StateFlow<ConnectionEvent> = _connectionEvent.asStateFlow()

        fun connect() {
            _connectionEvent.value = ConnectionEvent.CONNECT
        }

        fun disconnect() {
            _connectionEvent.value = ConnectionEvent.DISCONNECT
        }
    }

    internal object Automatic : ConnectionController()
}