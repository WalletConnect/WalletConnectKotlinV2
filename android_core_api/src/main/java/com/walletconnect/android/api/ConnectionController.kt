@file:JvmSynthetic

package com.walletconnect.android.api

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class ConnectionController {

    class Manual : ConnectionController() {
        private val _connectionEvent: MutableStateFlow<ConnectionEvent> = MutableStateFlow(ConnectionEvent.DISCONNECT)
        val connectionEventFlow: StateFlow<ConnectionEvent> = _connectionEvent.asStateFlow()

        fun connect() {
            _connectionEvent.value = ConnectionEvent.CONNECT
        }

        fun disconnect() {
            _connectionEvent.value = ConnectionEvent.DISCONNECT
        }
    }

    object Automatic : ConnectionController()
}