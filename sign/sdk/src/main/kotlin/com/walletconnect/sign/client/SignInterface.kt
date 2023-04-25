package com.walletconnect.sign.client

interface SignInterface {
    interface WalletDelegate {
        fun onSessionProposal(sessionProposal: Sign.Model.SessionProposal, sessionContext: Sign.Model.SessionContext)
        fun onSessionRequest(sessionRequest: Sign.Model.SessionRequest)
        fun onSessionDelete(deletedSession: Sign.Model.DeletedSession)

        //Responses
        fun onSessionSettleResponse(settleSessionResponse: Sign.Model.SettledSessionResponse)
        fun onSessionUpdateResponse(sessionUpdateResponse: Sign.Model.SessionUpdateResponse)

        //Utils
        fun onConnectionStateChange(state: Sign.Model.ConnectionState)
        fun onError(error: Sign.Model.Error)
    }

    interface DappDelegate {
        fun onSessionApproved(approvedSession: Sign.Model.ApprovedSession)
        fun onSessionRejected(rejectedSession: Sign.Model.RejectedSession)
        fun onSessionUpdate(updatedSession: Sign.Model.UpdatedSession)
        fun onSessionEvent(sessionEvent: Sign.Model.SessionEvent)
        fun onSessionExtend(session: Sign.Model.Session)
        fun onSessionDelete(deletedSession: Sign.Model.DeletedSession)

        //Responses
        fun onSessionRequestResponse(response: Sign.Model.SessionRequestResponse)

        // Utils
        fun onConnectionStateChange(state: Sign.Model.ConnectionState)
        fun onError(error: Sign.Model.Error)
    }

    fun initialize(init: Sign.Params.Init, onSuccess: () -> Unit = {}, onError: (Sign.Model.Error) -> Unit)
    fun setWalletDelegate(delegate: WalletDelegate)
    fun setDappDelegate(delegate: DappDelegate)

    fun connect(
        connect: Sign.Params.Connect, onSuccess: () -> Unit,
        onError: (Sign.Model.Error) -> Unit
    )

    @Deprecated(
        message = "Creating a pairing will be moved to CoreClient to make pairing SDK agnostic",
        replaceWith = ReplaceWith(expression = "CoreClient.Pairing.pair()", imports = ["com.walletconnect.android.CoreClient"])
    )
    fun pair(pair: Sign.Params.Pair, onSuccess: (Sign.Params.Pair) -> Unit = {}, onError: (Sign.Model.Error) -> Unit)
    fun approveSession(approve: Sign.Params.Approve, onSuccess: (Sign.Params.Approve) -> Unit = {}, onError: (Sign.Model.Error) -> Unit)
    fun rejectSession(reject: Sign.Params.Reject, onSuccess: (Sign.Params.Reject) -> Unit = {}, onError: (Sign.Model.Error) -> Unit)

    @Deprecated(
        message = "The onSuccess callback has been replaced with a new callback that returns Sign.Model.SentRequest",
        replaceWith = ReplaceWith(expression = "this.request(request, onSuccessWithSentRequest, onError)", imports = ["com.walletconnect.sign.client"])
    )
    fun request(
        request: Sign.Params.Request,
        onSuccess: (Sign.Params.Request) -> Unit = {},
        onSuccessWithSentRequest: (Sign.Model.SentRequest) -> Unit = { it: Sign.Model.SentRequest -> Unit },
        onError: (Sign.Model.Error) -> Unit
    )

    fun request(request: Sign.Params.Request, onSuccess: (Sign.Model.SentRequest) -> Unit = {}, onError: (Sign.Model.Error) -> Unit)
    fun respond(response: Sign.Params.Response, onSuccess: (Sign.Params.Response) -> Unit = {}, onError: (Sign.Model.Error) -> Unit)
    fun update(update: Sign.Params.Update, onSuccess: (Sign.Params.Update) -> Unit = {}, onError: (Sign.Model.Error) -> Unit)
    fun extend(extend: Sign.Params.Extend, onSuccess: (Sign.Params.Extend) -> Unit = {}, onError: (Sign.Model.Error) -> Unit)
    fun emit(emit: Sign.Params.Emit, onSuccess: (Sign.Params.Emit) -> Unit = {}, onError: (Sign.Model.Error) -> Unit)
    fun ping(ping: Sign.Params.Ping, sessionPing: Sign.Listeners.SessionPing? = null)
    fun disconnect(disconnect: Sign.Params.Disconnect, onSuccess: (Sign.Params.Disconnect) -> Unit = {}, onError: (Sign.Model.Error) -> Unit)

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    fun getListOfActiveSessions(): List<Sign.Model.Session>

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
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
    fun getPendingSessionRequests(topic: String): List<Sign.Model.SessionRequest>

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    fun getSessionProposals(): List<Sign.Model.SessionProposal>
}