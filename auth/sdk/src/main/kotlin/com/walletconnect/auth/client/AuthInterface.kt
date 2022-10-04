package com.walletconnect.auth.client

interface AuthInterface {

    sealed interface AuthDelegate {
        fun onConnectionStateChange(connectionStateChange: Auth.Event.ConnectionStateChange)
        fun onError(error: Auth.Event.Error)
    }

    interface RequesterDelegate : AuthDelegate {
        fun onAuthResponse(authResponse: Auth.Event.AuthResponse)
    }

    interface ResponderDelegate : AuthDelegate {
        fun onAuthRequest(authRequest: Auth.Event.AuthRequest)
    }

    fun setRequesterDelegate(delegate: RequesterDelegate)

    fun setResponderDelegate(delegate: ResponderDelegate)

    fun initialize(init: Auth.Params.Init, onError: (Auth.Model.Error) -> Unit)

    fun request(params: Auth.Params.Request, onPairing: (Auth.Model.Pairing) -> Unit, onError: (Auth.Model.Error) -> Unit)

    fun respond(params: Auth.Params.Respond, onError: (Auth.Model.Error) -> Unit)

    fun getPendingRequest(): List<Auth.Model.PendingRequest>
}