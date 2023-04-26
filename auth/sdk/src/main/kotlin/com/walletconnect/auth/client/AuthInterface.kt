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
        fun onAuthRequest(authRequest: Auth.Event.AuthRequest, authContext: Auth.Event.AuthContext)
    }

    fun setRequesterDelegate(delegate: RequesterDelegate)

    fun setResponderDelegate(delegate: ResponderDelegate)

    fun initialize(params: Auth.Params.Init, onSuccess: () -> Unit = {}, onError: (Auth.Model.Error) -> Unit)

    fun request(params: Auth.Params.Request, onSuccess: () -> Unit, onError: (Auth.Model.Error) -> Unit)

    fun respond(params: Auth.Params.Respond, onSuccess: (Auth.Params.Respond) -> Unit = {}, onError: (Auth.Model.Error) -> Unit)

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    fun formatMessage(params: Auth.Params.FormatMessage): String?

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    fun getPendingRequest(): List<Auth.Model.PendingRequest>
}