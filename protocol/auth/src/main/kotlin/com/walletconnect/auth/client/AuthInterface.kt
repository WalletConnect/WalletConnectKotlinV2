package com.walletconnect.auth.client

interface AuthInterface {

    @Deprecated("AuthSDK has been deprecated. Please use updated Web3Wallet and Sign SDKs instead.")
    sealed interface AuthDelegate {
        @Deprecated("AuthSDK has been deprecated. Please use updated Web3Wallet and Sign SDKs instead.")
        fun onConnectionStateChange(connectionStateChange: Auth.Event.ConnectionStateChange)

        @Deprecated("AuthSDK has been deprecated. Please use updated Web3Wallet and Sign SDKs instead.")
        fun onError(error: Auth.Event.Error)
    }

    @Deprecated("AuthSDK has been deprecated. Please use updated Web3Wallet and Sign SDKs instead.")
    interface RequesterDelegate : AuthDelegate {
        @Deprecated(
            "AuthSDK has been deprecated. Please use updated Web3Wallet and Sign SDKs instead.",
            replaceWith = ReplaceWith("fun onSessionAuthenticateResponse(sessionAuthenticateResponse: Sign.Model.SessionAuthenticateResponse) in Sign SDK")
        )
        fun onAuthResponse(authResponse: Auth.Event.AuthResponse)
    }

    interface ResponderDelegate : AuthDelegate {
        @Deprecated(
            "AuthSDK has been deprecated. Please use updated Web3Wallet and Sign SDKs instead.",
            replaceWith = ReplaceWith("fun onSessionAuthenticated(sessionAuthenticate: Wallet.Model.SessionAuthenticate, verifyContext: Wallet.Model.VerifyContext)")
        )
        fun onAuthRequest(authRequest: Auth.Event.AuthRequest, verifyContext: Auth.Event.VerifyContext)
    }

    @Deprecated("AuthSDK has been deprecated. Please use updated Web3Wallet and Sign SDKs instead.")
    fun setRequesterDelegate(delegate: RequesterDelegate)

    @Deprecated("AuthSDK has been deprecated. Please use updated Web3Wallet and Sign SDKs instead.")
    fun setResponderDelegate(delegate: ResponderDelegate)

    @Deprecated("AuthSDK has been deprecated. Please use updated Web3Wallet and Sign SDKs instead.")
    fun initialize(params: Auth.Params.Init, onSuccess: () -> Unit = {}, onError: (Auth.Model.Error) -> Unit)

    @Deprecated(
        "AuthSDK has been deprecated. Please use updated Web3Wallet and Sign SDKs instead.",
        replaceWith = ReplaceWith("fun sessionAuthenticate(authenticate: Sign.Params.Authenticate, onSuccess: (String) -> Unit, onError: (Sign.Model.Error) -> Unit) in Sign SDK")
    )
    fun request(params: Auth.Params.Request, onSuccess: () -> Unit, onError: (Auth.Model.Error) -> Unit)

    @Deprecated(
        "AuthSDK has been deprecated. Please use updated Web3Wallet and Sign SDKs instead.",
        replaceWith = ReplaceWith("fun approveSessionAuthenticated(approve: Sign.Params.ApproveSessionAuthenticate, onSuccess: (Sign.Params.ApproveSessionAuthenticate) -> Unit, onError: (Sign.Model.Error) -> Unit) or fun rejectSessionAuthenticated(reject: Sign.Params.RejectSessionAuthenticate, onSuccess: (Sign.Params.RejectSessionAuthenticate) -> Unit, onError: (Sign.Model.Error) -> Unit) in Web3Wallet SDK")
    )
    fun respond(params: Auth.Params.Respond, onSuccess: (Auth.Params.Respond) -> Unit = {}, onError: (Auth.Model.Error) -> Unit)

    @Deprecated(
        "AuthSDK has been deprecated. Please use updated Web3Wallet and Sign SDKs instead.",
        replaceWith = ReplaceWith("fun decryptMessage(params: Sign.Params.DecryptMessage, onSuccess: (Sign.Model.Message) -> Unit, onError: (Sign.Model.Error) -> Unit) in Web3Wallet SDK")
    )
    fun decryptMessage(params: Auth.Params.DecryptMessage, onSuccess: (Auth.Model.Message.AuthRequest) -> Unit, onError: (Auth.Model.Error) -> Unit)

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    @Deprecated(
        "AuthSDK has been deprecated. Please use updated Web3Wallet and Sign SDKs instead.",
        replaceWith = ReplaceWith("fun formatAuthMessage(formatMessage: Sign.Params.FormatMessage): String? in Web3Wallet SDK")
    )
    fun formatMessage(params: Auth.Params.FormatMessage): String?

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    @Deprecated(
        "AuthSDK has been deprecated. Please use updated Web3Wallet and Sign SDKs instead.",
        replaceWith = ReplaceWith("fun getPendingAuthenticateRequests(): List<Sign.Model.SessionAuthenticate> in Web3Wallet SDK")
    )
    fun getPendingRequest(): List<Auth.Model.PendingRequest>

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    @Deprecated(
        "AuthSDK has been deprecated. Please use updated Web3Wallet and Sign SDKs instead.",
        replaceWith = ReplaceWith("override fun getVerifyContext(id: Long): Sign.Model.VerifyContext? in Web3Wallet SDK")
    )
    fun getVerifyContext(id: Long): Auth.Model.VerifyContext?

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    @Deprecated(
        "AuthSDK has been deprecated. Please use updated Web3Wallet and Sign SDKs instead.",
        replaceWith = ReplaceWith("override fun getListOfVerifyContexts(): List<Sign.Model.VerifyContext> in Web3Wallet SDK")
    )
    fun getListOfVerifyContexts(): List<Auth.Model.VerifyContext>
}