package com.walletconnect.auth.client

interface AuthInterface {

    sealed interface AuthDelegate {
        fun onConnectionStateChange(connectionStateChange: Auth.Events.ConnectionStateChange)
        fun onError(error: Auth.Events.Error)
    }

    interface RequesterDelegate : AuthDelegate {
        fun onAuthResponse(authResponse: Auth.Model.AuthResponse)
    }

    interface ResponderDelegate : AuthDelegate {
        fun onAuthRequest(authRequest: Auth.Model.AuthRequest)
    }

    fun setRequesterDelegate(delegate: RequesterDelegate)

    fun setResponderDelegate(delegate: ResponderDelegate)

    fun initialize(init: Auth.Params.Init, onError: (Auth.Model.Error) -> Unit)

    fun pair(pair: Auth.Params.Pair, onError: (Auth.Model.Error) -> Unit)

    fun request(params: Auth.Params.Request, onPairing: (Auth.Model.Pairing) -> Unit, onError: (Auth.Model.Error) -> Unit)

    fun respond(params: Auth.Params.Respond, onError: (Auth.Model.Error) -> Unit)

    fun getPendingRequest(): Map<Int, Auth.Model.PendingRequest>

    fun getRequest(params: Auth.Params.RequestId): Auth.Model.Cacao
}