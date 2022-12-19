@file:JvmSynthetic

package com.walletconnect.foundation.network.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class ConnectionController {

    class Manual : ConnectionController() {
        private val _connectionEvent: MutableStateFlow<ConnectionEvent> = MutableStateFlow(ConnectionEvent.DISCONNECT)
        val connectionEventFlow: StateFlow<ConnectionEvent> = _connectionEvent.asStateFlow()

        fun connect() {
            println("kobe; connect controller")
            _connectionEvent.value = ConnectionEvent.CONNECT
        }

        fun disconnect() {
            _connectionEvent.value = ConnectionEvent.DISCONNECT
        }
    }

    object Automatic : ConnectionController()
}