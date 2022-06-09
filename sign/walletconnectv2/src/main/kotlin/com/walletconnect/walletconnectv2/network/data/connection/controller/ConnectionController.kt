package com.walletconnect.walletconnectv2.network.data.connection.controller

import com.walletconnect.walletconnectv2.network.data.connection.ConnectionEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal sealed class ConnectionController {

    internal class Manual : ConnectionController() {

        private val _connectionEvent: MutableStateFlow<ConnectionEvent> = MutableStateFlow(ConnectionEvent.DISCONNECT)

        val connectionEventFlow: StateFlow<ConnectionEvent> = _connectionEvent

        fun connect() {
            _connectionEvent.value = ConnectionEvent.CONNECT
        }

        fun disconnect() {
            _connectionEvent.value = ConnectionEvent.DISCONNECT
        }
    }

    internal object Automatic : ConnectionController()
}