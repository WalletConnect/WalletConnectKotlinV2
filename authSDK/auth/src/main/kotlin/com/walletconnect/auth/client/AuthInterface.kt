package com.walletconnect.auth.client

interface AuthInterface {

    sealed interface AuthDelegate {
        fun onConnectionStateChange(connectionStateChange: Auth.Events.ConnectionStateChange)
        fun onError(error: Auth.Events.Error)
    }

    interface RequesterDelegate : AuthDelegate {
        fun onAuthResponse(authResponse: Auth.Events.AuthResponse)
    }

    interface ResponderDelegate : AuthDelegate {
        fun onAuthRequest(authRequest: Auth.Events.AuthRequest)
    }

    fun setRequesterDelegate(delegate: RequesterDelegate)

    fun setResponderDelegate(delegate: ResponderDelegate)

    fun initialize(init: Auth.Params.Init, onError: (Auth.Model.Error) -> Unit)

    fun pair(pair: Auth.Params.Pair, onError: (Auth.Model.Error) -> Unit)

    fun request(params: Auth.Params.Request, onPairing: (Auth.Model.Pairing) -> Unit, onError: (Auth.Model.Error) -> Unit)

    fun respond(params: Auth.Params.Respond, onError: (Auth.Model.Error) -> Unit)

    fun getPendingRequest(): List<Auth.Model.PendingRequest>

    fun getResponse(params: Auth.Params.RequestId): Auth.Model.Response?
}