package com.walletconnect.sign.client

@Deprecated("com.walletconnect.sign.client.SignInterface has been deprecated. Please use com.reown.sign.client.SignInterface instead from - https://github.com/reown-com/reown-kotlin")
interface SignInterface {
    @Deprecated("com.walletconnect.sign.client.WalletDelegate has been deprecated. Please use com.reown.sign.client.WalletDelegate instead from - https://github.com/reown-com/reown-kotlin")
    interface WalletDelegate {
        fun onSessionProposal(sessionProposal: Sign.Model.SessionProposal, verifyContext: Sign.Model.VerifyContext)
        val onSessionAuthenticate: ((Sign.Model.SessionAuthenticate, Sign.Model.VerifyContext) -> Unit)? get() = null
        fun onSessionRequest(sessionRequest: Sign.Model.SessionRequest, verifyContext: Sign.Model.VerifyContext)
        fun onSessionDelete(deletedSession: Sign.Model.DeletedSession)
        fun onSessionExtend(session: Sign.Model.Session)

        //Responses
        fun onSessionSettleResponse(settleSessionResponse: Sign.Model.SettledSessionResponse)
        fun onSessionUpdateResponse(sessionUpdateResponse: Sign.Model.SessionUpdateResponse)

        //Utils
        fun onProposalExpired(proposal: Sign.Model.ExpiredProposal) {
            //override me
        }

        fun onRequestExpired(request: Sign.Model.ExpiredRequest) {
            //override me
        }

        fun onConnectionStateChange(state: Sign.Model.ConnectionState)
        fun onError(error: Sign.Model.Error)
    }

    @Deprecated("com.walletconnect.sign.client.DappDelegate has been deprecated. Please use com.reown.sign.client.DappDelegate instead from - https://github.com/reown-com/reown-kotlin")
    interface DappDelegate {
        fun onSessionApproved(approvedSession: Sign.Model.ApprovedSession)
        fun onSessionRejected(rejectedSession: Sign.Model.RejectedSession)
        fun onSessionUpdate(updatedSession: Sign.Model.UpdatedSession)

        @Deprecated(

            message = "onSessionEvent is deprecated. Use onEvent instead. Using both will result in duplicate events.",
            replaceWith = ReplaceWith(expression = "onEvent(event)")
        )
        fun onSessionEvent(sessionEvent: Sign.Model.SessionEvent)
        fun onSessionEvent(sessionEvent: Sign.Model.Event) {}
        fun onSessionExtend(session: Sign.Model.Session)
        fun onSessionDelete(deletedSession: Sign.Model.DeletedSession)

        //Responses
        fun onSessionRequestResponse(response: Sign.Model.SessionRequestResponse)
        fun onSessionAuthenticateResponse(sessionAuthenticateResponse: Sign.Model.SessionAuthenticateResponse) {}

        // Utils
        fun onProposalExpired(proposal: Sign.Model.ExpiredProposal)
        fun onRequestExpired(request: Sign.Model.ExpiredRequest)
        fun onConnectionStateChange(state: Sign.Model.ConnectionState)
        fun onError(error: Sign.Model.Error)
    }

    @Deprecated("com.walletconnect.sign.client.SignClient has been deprecated. Please use com.reown.sign.client.SignClient instead from - https://github.com/reown-com/reown-kotlin")
    fun initialize(init: Sign.Params.Init, onSuccess: () -> Unit = {}, onError: (Sign.Model.Error) -> Unit)
    @Deprecated("com.walletconnect.sign.client.SignClient has been deprecated. Please use com.reown.sign.client.SignClient instead from - https://github.com/reown-com/reown-kotlin")
    fun setWalletDelegate(delegate: WalletDelegate)
    @Deprecated("com.walletconnect.sign.client.SignClient has been deprecated. Please use com.reown.sign.client.SignClient instead from - https://github.com/reown-com/reown-kotlin")
    fun setDappDelegate(delegate: DappDelegate)

    @Deprecated(
        message = "Replaced with the same name method but onSuccess callback returns a Pairing URL",
        replaceWith = ReplaceWith(expression = "fun connect(connect: Sign.Params.Connect, onSuccess: (String) -> Unit, onError: (Sign.Model.Error) -> Unit)")
    )
    fun connect(
        connect: Sign.Params.Connect, onSuccess: () -> Unit,
        onError: (Sign.Model.Error) -> Unit,
    )

    @Deprecated("com.walletconnect.sign.client.SignClient has been deprecated. Please use com.reown.sign.client.SignClient instead from - https://github.com/reown-com/reown-kotlin")
    fun connect(
        connect: Sign.Params.Connect, onSuccess: (String) -> Unit,
        onError: (Sign.Model.Error) -> Unit,
    )

    @Deprecated("com.walletconnect.sign.client.SignClient has been deprecated. Please use com.reown.sign.client.SignClient instead from - https://github.com/reown-com/reown-kotlin")
    fun authenticate(authenticate: Sign.Params.Authenticate, walletAppLink: String? = null, onSuccess: (String) -> Unit, onError: (Sign.Model.Error) -> Unit)
    @Deprecated("com.walletconnect.sign.client.SignClient has been deprecated. Please use com.reown.sign.client.SignClient instead from - https://github.com/reown-com/reown-kotlin")
    fun dispatchEnvelope(urlWithEnvelope: String, onError: (Sign.Model.Error) -> Unit)

    @Deprecated(
        message = "Creating a pairing will be moved to CoreClient to make pairing SDK agnostic",
        replaceWith = ReplaceWith(expression = "CoreClient.Pairing.pair()", imports = ["com.walletconnect.android.CoreClient"])
    )
    fun pair(pair: Sign.Params.Pair, onSuccess: (Sign.Params.Pair) -> Unit = {}, onError: (Sign.Model.Error) -> Unit)
    @Deprecated("com.walletconnect.sign.client.SignClient has been deprecated. Please use com.reown.sign.client.SignClient instead from - https://github.com/reown-com/reown-kotlin")
    fun approveSession(approve: Sign.Params.Approve, onSuccess: (Sign.Params.Approve) -> Unit = {}, onError: (Sign.Model.Error) -> Unit)
    @Deprecated("com.walletconnect.sign.client.SignClient has been deprecated. Please use com.reown.sign.client.SignClient instead from - https://github.com/reown-com/reown-kotlin")
    fun rejectSession(reject: Sign.Params.Reject, onSuccess: (Sign.Params.Reject) -> Unit = {}, onError: (Sign.Model.Error) -> Unit)
    @Deprecated("com.walletconnect.sign.client.SignClient has been deprecated. Please use com.reown.sign.client.SignClient instead from - https://github.com/reown-com/reown-kotlin")
    fun approveAuthenticate(approve: Sign.Params.ApproveAuthenticate, onSuccess: (Sign.Params.ApproveAuthenticate) -> Unit, onError: (Sign.Model.Error) -> Unit)
    @Deprecated("com.walletconnect.sign.client.SignClient has been deprecated. Please use com.reown.sign.client.SignClient instead from - https://github.com/reown-com/reown-kotlin")
    fun rejectAuthenticate(reject: Sign.Params.RejectAuthenticate, onSuccess: (Sign.Params.RejectAuthenticate) -> Unit, onError: (Sign.Model.Error) -> Unit)
    @Deprecated("com.walletconnect.sign.client.SignClient has been deprecated. Please use com.reown.sign.client.SignClient instead from - https://github.com/reown-com/reown-kotlin")
    fun formatAuthMessage(formatMessage: Sign.Params.FormatMessage): String

    @Deprecated(
        message = "The onSuccess callback has been replaced with a new callback that returns Sign.Model.SentRequest",
        replaceWith = ReplaceWith(expression = "this.request(request, onSuccessWithSentRequest, onError)", imports = ["com.walletconnect.sign.client"])
    )
    fun request(
        request: Sign.Params.Request,
        onSuccess: (Sign.Params.Request) -> Unit = {},
        onSuccessWithSentRequest: (Sign.Model.SentRequest) -> Unit = { it: Sign.Model.SentRequest -> Unit },
        onError: (Sign.Model.Error) -> Unit,
    )
    @Deprecated("com.walletconnect.sign.client.SignClient has been deprecated. Please use com.reown.sign.client.SignClient instead from - https://github.com/reown-com/reown-kotlin")
    fun request(request: Sign.Params.Request, onSuccess: (Sign.Model.SentRequest) -> Unit = {}, onError: (Sign.Model.Error) -> Unit)
    @Deprecated("com.walletconnect.sign.client.SignClient has been deprecated. Please use com.reown.sign.client.SignClient instead from - https://github.com/reown-com/reown-kotlin")
    fun respond(response: Sign.Params.Response, onSuccess: (Sign.Params.Response) -> Unit = {}, onError: (Sign.Model.Error) -> Unit)
    @Deprecated("com.walletconnect.sign.client.SignClient has been deprecated. Please use com.reown.sign.client.SignClient instead from - https://github.com/reown-com/reown-kotlin")
    fun update(update: Sign.Params.Update, onSuccess: (Sign.Params.Update) -> Unit = {}, onError: (Sign.Model.Error) -> Unit)
    @Deprecated("com.walletconnect.sign.client.SignClient has been deprecated. Please use com.reown.sign.client.SignClient instead from - https://github.com/reown-com/reown-kotlin")
    fun extend(extend: Sign.Params.Extend, onSuccess: (Sign.Params.Extend) -> Unit = {}, onError: (Sign.Model.Error) -> Unit)
    @Deprecated("com.walletconnect.sign.client.SignClient has been deprecated. Please use com.reown.sign.client.SignClient instead from - https://github.com/reown-com/reown-kotlin")
    fun emit(emit: Sign.Params.Emit, onSuccess: (Sign.Params.Emit) -> Unit = {}, onError: (Sign.Model.Error) -> Unit)
    @Deprecated("com.walletconnect.sign.client.SignClient has been deprecated. Please use com.reown.sign.client.SignClient instead from - https://github.com/reown-com/reown-kotlin")
    fun ping(ping: Sign.Params.Ping, sessionPing: Sign.Listeners.SessionPing? = null)
    @Deprecated("com.walletconnect.sign.client.SignClient has been deprecated. Please use com.reown.sign.client.SignClient instead from - https://github.com/reown-com/reown-kotlin")
    fun disconnect(disconnect: Sign.Params.Disconnect, onSuccess: (Sign.Params.Disconnect) -> Unit = {}, onError: (Sign.Model.Error) -> Unit)
    @Deprecated("com.walletconnect.sign.client.SignClient has been deprecated. Please use com.reown.sign.client.SignClient instead from - https://github.com/reown-com/reown-kotlin")
    fun decryptMessage(params: Sign.Params.DecryptMessage, onSuccess: (Sign.Model.Message) -> Unit, onError: (Sign.Model.Error) -> Unit)

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    @Deprecated("com.walletconnect.sign.client.SignClient has been deprecated. Please use com.reown.sign.client.SignClient instead from - https://github.com/reown-com/reown-kotlin")
    fun getListOfActiveSessions(): List<Sign.Model.Session>

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    @Deprecated("com.walletconnect.sign.client.SignClient has been deprecated. Please use com.reown.sign.client.SignClient instead from - https://github.com/reown-com/reown-kotlin")
    fun getActiveSessionByTopic(topic: String): Sign.Model.Session?

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    @Deprecated(
        message = "Getting a list of settled sessions is replaced with getListOfActiveSessions()",
        replaceWith = ReplaceWith(expression = "SignClient.getListOfActiveSessions()")
    )
    fun getListOfSettledSessions(): List<Sign.Model.Session>

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    @Deprecated(
        message = "Getting a list of settled sessions by topic is replaced with getSettledSessionByTopic()",
        replaceWith = ReplaceWith(expression = "SignClient.getSettledSessionByTopic()")
    )
    fun getSettledSessionByTopic(topic: String): Sign.Model.Session?

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    @Deprecated(
        message = "Getting a list of Pairings will be moved to CoreClient to make pairing SDK agnostic",
        replaceWith = ReplaceWith(expression = "CoreClient.Pairing.getPairings()")
    )
    fun getListOfSettledPairings(): List<Sign.Model.Pairing>

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    @Deprecated(
        "The return type of getPendingRequests methods has been replaced with SessionRequest list",
        replaceWith = ReplaceWith("getPendingSessionRequests(topic: String): List<Sign.Model.SessionRequest>")
    )
    fun getPendingRequests(topic: String): List<Sign.Model.PendingRequest>

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    @Deprecated("com.walletconnect.sign.client.SignClient has been deprecated. Please use com.reown.sign.client.SignClient instead from - https://github.com/reown-com/reown-kotlin")
    fun getPendingSessionRequests(topic: String): List<Sign.Model.SessionRequest>

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    @Deprecated("com.walletconnect.sign.client.SignClient has been deprecated. Please use com.reown.sign.client.SignClient instead from - https://github.com/reown-com/reown-kotlin")
    fun getSessionProposals(): List<Sign.Model.SessionProposal>

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    @Deprecated("com.walletconnect.sign.client.SignClient has been deprecated. Please use com.reown.sign.client.SignClient instead from - https://github.com/reown-com/reown-kotlin")
    fun getVerifyContext(id: Long): Sign.Model.VerifyContext?

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    @Deprecated("com.walletconnect.sign.client.SignClient has been deprecated. Please use com.reown.sign.client.SignClient instead from - https://github.com/reown-com/reown-kotlin")
    fun getPendingAuthenticateRequests(): List<Sign.Model.SessionAuthenticate>

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    @Deprecated("com.walletconnect.sign.client.SignClient has been deprecated. Please use com.reown.sign.client.SignClient instead from - https://github.com/reown-com/reown-kotlin")
    fun getListOfVerifyContexts(): List<Sign.Model.VerifyContext>
}