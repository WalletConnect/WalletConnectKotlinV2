package com.walletconnect.requester.domain

import com.walletconnect.auth.client.Auth
import com.walletconnect.auth.client.AuthClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

object RequesterDelegate : AuthClient.RequesterDelegate {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _wcEvents: MutableSharedFlow<Auth.Events> = MutableSharedFlow()
    val wcEvents: SharedFlow<Auth.Events> = _wcEvents.asSharedFlow()

    init {
        AuthClient.setRequesterDelegate(this)
    }

    override fun onAuthResponse(authResponse: Auth.Events.AuthResponse) {
        scope.launch {
            _wcEvents.emit(authResponse)
        }
    }

    override fun onConnectionStateChange(connectionStateChange: Auth.Events.ConnectionStateChange) {
        scope.launch {
            _wcEvents.emit(connectionStateChange)
        }
    }

    override fun onError(error: Auth.Events.Error) {
        scope.launch {
            _wcEvents.emit(error)
        }
    }
}