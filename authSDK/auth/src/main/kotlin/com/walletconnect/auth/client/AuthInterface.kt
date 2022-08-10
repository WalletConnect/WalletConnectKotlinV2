package com.walletconnect.auth.client

interface AuthInterface {
    interface AuthDelegate {
        fun onAuthRequest(authRequest: Auth.Model.AuthRequest)
        fun onAuthResponse(authResponse: Auth.Model.AuthResponse)
    }

    fun setAuthDelegate(delegate: AuthDelegate)

    fun initialize(init: Auth.Params.Init, onError: (Auth.Model.Error) -> Unit)

    fun pair(pair: Auth.Params.Pair, onError: (Auth.Model.Error) -> Unit)

    fun request(params: Auth.Params.Request)

    fun respond(params: Auth.Params.Respond)

    fun getPendingRequest(): Map<Int, Auth.Model.PendingRequest>

    fun getRequest(params: Auth.Params.RequestId): Auth.Model.Cacao
}