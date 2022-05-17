package com.walletconnect.walletconnectv2.network.data.connection.controller

import com.walletconnect.walletconnectv2.network.data.connection.ConnectionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal sealed class ConnectionController {

    internal class Manual : ConnectionController() {

        private val _connectionState: MutableStateFlow<ConnectionState> = MutableStateFlow(ConnectionState.DISCONNECT)

        val connectionStateFlow: StateFlow<ConnectionState> = _connectionState

        fun connect() {
            _connectionState.value = ConnectionState.CONNECT
        }

        fun disconnect() {
            _connectionState.value = ConnectionState.DISCONNECT
        }
    }

    internal object Automatic : ConnectionController()
}