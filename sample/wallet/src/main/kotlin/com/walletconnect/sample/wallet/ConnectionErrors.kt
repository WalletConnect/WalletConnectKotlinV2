package com.walletconnect.sample.wallet

import kotlinx.coroutines.flow.MutableStateFlow

val connectionStateFlow: MutableStateFlow<ConnectionState> = MutableStateFlow(ConnectionState.Idle)

sealed class ConnectionState() {
    data class Error(val message: String) : ConnectionState()
    object Ok : ConnectionState()
    object Idle : ConnectionState()
}