package com.walletconnect.wcmodal.client

import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.client.SignClient
import com.walletconnect.sign.common.exceptions.SignClientAlreadyInitializedException
import com.walletconnect.wcmodal.di.walletConnectModalModule
import com.walletconnect.wcmodal.domain.WalletConnectModalDelegate
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

object WalletConnectModal {

    internal var excludedWalletsIds: List<String> = listOf()
    internal var recommendedWalletsIds: List<String> = listOf()

    private var _sessionParams: Modal.Params.SessionParams? = null
    val sessionParams: Modal.Params.SessionParams
        get() = requireNotNull(_sessionParams) { "Be sure to set the SessionParams in either the Modal.Params.Init or WalletConnectModal.setSessionParams." }

    interface ModalDelegate {
        fun onSessionApproved(approvedSession: Modal.Model.ApprovedSession)
        fun onSessionRejected(rejectedSession: Modal.Model.RejectedSession)
        fun onSessionUpdate(updatedSession: Modal.Model.UpdatedSession)
        fun onSessionEvent(sessionEvent: Modal.Model.SessionEvent)
        fun onSessionExtend(session: Modal.Model.Session)
        fun onSessionDelete(deletedSession: Modal.Model.DeletedSession)

        //Responses
        fun onSessionRequestResponse(response: Modal.Model.SessionRequestResponse)
        fun onSessionAuthenticateResponse(sessionUpdateResponse: Modal.Model.SessionAuthenticateResponse) {}

        // Utils
        fun onProposalExpired(proposal: Modal.Model.ExpiredProposal)
        fun onRequestExpired(request: Modal.Model.ExpiredRequest)
        fun onConnectionStateChange(state: Modal.Model.ConnectionState)
        fun onError(error: Modal.Model.Error)
    }

    fun initialize(
        init: Modal.Params.Init,
        onSuccess: () -> Unit = {},
        onError: (Modal.Model.Error) -> Unit
    ) {
        SignClient.initialize(
            init = Sign.Params.Init(init.core),
            onSuccess = { onInitializedClient(init, onSuccess, onError) },
            onError = { error ->
                if (error.throwable is SignClientAlreadyInitializedException) {
                    onInitializedClient(init, onSuccess, onError)
                } else {
                    onError(Modal.Model.Error(error.throwable))
                    return@initialize
                }
            }
        )
    }

    private fun onInitializedClient(
        init: Modal.Params.Init,
        onSuccess: () -> Unit = {},
        onError: (Modal.Model.Error) -> Unit
    ) {
        this.excludedWalletsIds = init.excludedWalletIds
        this.recommendedWalletsIds = init.recommendedWalletsIds
        _sessionParams = init.sessionParams
        runCatching {
            wcKoinApp.modules(walletConnectModalModule())
            setInternalDelegate(WalletConnectModalDelegate)
        }.onFailure { error -> return@onInitializedClient onError(Modal.Model.Error(error)) }
        onSuccess()
    }

    @Throws(IllegalStateException::class)
    fun setDelegate(delegate: ModalDelegate) {
        WalletConnectModalDelegate.wcEventModels.onEach { event ->
            when (event) {
                is Modal.Model.ApprovedSession -> delegate.onSessionApproved(event)
                is Modal.Model.ConnectionState -> delegate.onConnectionStateChange(event)
                is Modal.Model.DeletedSession.Success -> delegate.onSessionDelete(event)
                is Modal.Model.Error -> delegate.onError(event)
                is Modal.Model.RejectedSession -> delegate.onSessionRejected(event)
                is Modal.Model.Session -> delegate.onSessionExtend(event)
                is Modal.Model.SessionEvent -> delegate.onSessionEvent(event)
                is Modal.Model.SessionRequestResponse -> delegate.onSessionRequestResponse(event)
                is Modal.Model.UpdatedSession -> delegate.onSessionUpdate(event)
                is Modal.Model.ExpiredProposal -> delegate.onProposalExpired(event)
                is Modal.Model.ExpiredRequest -> delegate.onRequestExpired(event)
                is Modal.Model.SessionAuthenticateResponse -> delegate.onSessionAuthenticateResponse(event)
                else -> Unit
            }
        }.launchIn(scope)
    }

    @Throws(IllegalStateException::class)
    private fun setInternalDelegate(delegate: ModalDelegate) {
        val signDelegate = object : SignClient.DappDelegate {
            override fun onSessionApproved(approvedSession: Sign.Model.ApprovedSession) {
                delegate.onSessionApproved(approvedSession.toModal())
            }

            override fun onSessionRejected(rejectedSession: Sign.Model.RejectedSession) {
                delegate.onSessionRejected(rejectedSession.toModal())
            }

            override fun onSessionUpdate(updatedSession: Sign.Model.UpdatedSession) {
                delegate.onSessionUpdate(updatedSession.toModal())
            }

            override fun onSessionEvent(sessionEvent: Sign.Model.SessionEvent) {
                delegate.onSessionEvent(sessionEvent.toModal())
            }

            override fun onSessionExtend(session: Sign.Model.Session) {
                delegate.onSessionExtend(session.toModal())
            }

            override fun onSessionDelete(deletedSession: Sign.Model.DeletedSession) {
                delegate.onSessionDelete(deletedSession.toModal())
            }

            override fun onSessionRequestResponse(response: Sign.Model.SessionRequestResponse) {
                delegate.onSessionRequestResponse(response.toModal())
            }

            override fun onProposalExpired(proposal: Sign.Model.ExpiredProposal) {
                delegate.onProposalExpired(proposal.toModal())
            }

            override fun onRequestExpired(request: Sign.Model.ExpiredRequest) {
                delegate.onRequestExpired(request.toModal())
            }

            override fun onSessionAuthenticateResponse(sessionAuthenticateResponse: Sign.Model.SessionAuthenticateResponse) {
                delegate.onSessionAuthenticateResponse(sessionAuthenticateResponse.toModal())
            }

            override fun onConnectionStateChange(state: Sign.Model.ConnectionState) {
                delegate.onConnectionStateChange(state.toModal())
            }

            override fun onError(error: Sign.Model.Error) {
                delegate.onError(error.toModal())
            }
        }
        SignClient.setDappDelegate(signDelegate)
    }

    fun setSessionParams(sessionParams: Modal.Params.SessionParams) {
        _sessionParams = sessionParams
    }

    @Deprecated(
        message = "Replaced with the same name method but onSuccess callback returns a Pairing URL",
        replaceWith = ReplaceWith(expression = "fun connect(connect: Modal.Params.Connect, onSuccess: (String) -> Unit, onError: (Modal.Model.Error) -> Unit)")
    )
    fun connect(
        connect: Modal.Params.Connect,
        onSuccess: () -> Unit,
        onError: (Modal.Model.Error) -> Unit
    ) {
        SignClient.connect(
            connect = connect.toSign(),
            onSuccess = onSuccess,
            onError = { onError(it.toModal()) }
        )
    }

    fun connect(
        connect: Modal.Params.Connect,
        onSuccess: (String) -> Unit,
        onError: (Modal.Model.Error) -> Unit
    ) {
        SignClient.connect(
            connect = connect.toSign(),
            onSuccess = { url -> onSuccess(url) },
            onError = { onError(it.toModal()) }
        )
    }

    fun authenticate(
        authenticate: Modal.Params.Authenticate,
        onSuccess: (String) -> Unit,
        onError: (Modal.Model.Error) -> Unit,
    ) {

        SignClient.authenticate(authenticate.toSign(),
            onSuccess = { url -> onSuccess(url) },
            onError = { onError(it.toModal()) })
    }

    fun request(request: Modal.Params.Request, onSuccess: (Modal.Model.SentRequest) -> Unit = {}, onError: (Modal.Model.Error) -> Unit) {
        SignClient.request(
            request.toSign(),
            { onSuccess(it.toModal()) },
            { onError(it.toModal()) }
        )
    }

    fun ping(ping: Modal.Params.Ping, sessionPing: Modal.Listeners.SessionPing? = null) {
        SignClient.ping(
            ping = ping.toSign(),
            sessionPing = sessionPing?.toSign()
        )
    }

    fun disconnect(disconnect: Modal.Params.Disconnect, onSuccess: (Modal.Params.Disconnect) -> Unit = {}, onError: (Modal.Model.Error) -> Unit) {
        SignClient.disconnect(
            disconnect = disconnect.toSign(),
            onSuccess = { onSuccess(it.toModal()) },
            onError = { onError(it.toModal()) }
        )
    }

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    fun getListOfActiveSessions() = SignClient.getListOfActiveSessions().map { it.toModal() }

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    fun getListOfProposals() = SignClient.getSessionProposals().map { it.toModal() }

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    fun getActiveSessionByTopic(topic: String) = SignClient.getActiveSessionByTopic(topic)?.toModal()

}
