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
    private val _wcEvents: MutableSharedFlow<Auth.Events> = MutableSharedFlow()
    val wcEvents: SharedFlow<Auth.Events> = _wcEvents.asSharedFlow()

    init {
        AuthClient.setResponderDelegate(this)
    }

    override fun onAuthRequest(authRequest: Auth.Events.AuthRequest) {
        scope.launch {
            _wcEvents.emit(authRequest)
        }
    }

    override fun onConnectionStateChange(state: Auth.Model.ConnectionState) {
        TODO("Not yet implemented")
    }

    override fun onError(error: Auth.Model.Error) {
        TODO("Not yet implemented")
    }
}