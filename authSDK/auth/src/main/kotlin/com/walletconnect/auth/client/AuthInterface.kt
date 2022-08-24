package com.walletconnect.auth.client

interface AuthInterface {

    sealed interface AuthDelegate {
        fun onConnectionStateChange(state: Auth.Model.ConnectionState)
        fun onError(error: Auth.Model.Error)
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

    fun request(params: Auth.Params.Request)

    fun respond(params: Auth.Params.Respond)

    fun getPendingRequest(): Map<Int, Auth.Model.PendingRequest>

    fun getRequest(params: Auth.Params.RequestId): Auth.Model.Cacao
}