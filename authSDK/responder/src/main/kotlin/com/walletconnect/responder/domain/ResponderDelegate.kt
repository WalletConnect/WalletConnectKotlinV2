package com.walletconnect.responder.domain

import com.walletconnect.auth.client.Auth
import com.walletconnect.auth.client.AuthClient
import com.walletconnect.auth.client.AuthInterface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

object ResponderDelegate: AuthInterface.ResponderDelegate {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _wcEvents: MutableSharedFlow<Auth.Event> = MutableSharedFlow()
    val wcEvents: SharedFlow<Auth.Event> = _wcEvents.asSharedFlow()

    init {
        AuthClient.setResponderDelegate(this)
    }

    override fun onAuthRequest(authRequest: Auth.Event.AuthRequest) {
        scope.launch {
            _wcEvents.emit(authRequest)
        }
    }

    override fun onConnectionStateChange(connectionStateChange: Auth.Event.ConnectionStateChange) {
        scope.launch {
            _wcEvents.emit(connectionStateChange)
        }
    }

    override fun onError(error: Auth.Event.Error) {
        scope.launch {
            _wcEvents.emit(error)
        }
    }
}