package com.walletconnect.wcmodal.client

import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.wcmodal.domain.WalletConnectModalDelegate
import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.client.SignClient
import com.walletconnect.wcmodal.di.walletConnectModalModule

object WalletConnectModal {

    internal var excludedWalletsIds: List<String> = listOf()
    internal var recommendedWalletsIds: List<String> = listOf()

    interface ModalDelegate {
        fun onSessionApproved(approvedSession: Modal.Model.ApprovedSession)
        fun onSessionRejected(rejectedSession: Modal.Model.RejectedSession)
        fun onSessionUpdate(updatedSession: Modal.Model.UpdatedSession)
        fun onSessionEvent(sessionEvent: Modal.Model.SessionEvent)
        fun onSessionExtend(session: Modal.Model.Session)
        fun onSessionDelete(deletedSession: Modal.Model.DeletedSession)

        //Responses
        fun onSessionRequestResponse(response: Modal.Model.SessionRequestResponse)

        // Utils
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
            onSuccess = {
                this.excludedWalletsIds = init.excludedWalletIds
                this.recommendedWalletsIds = init.recommendedWalletsIds
                runCatching {
                    wcKoinApp.modules(walletConnectModalModule())
                    setDelegate(WalletConnectModalDelegate)
                }.onFailure { error -> onError(Modal.Model.Error(error)) }
                onSuccess()
            },
            onError = { error ->
                onError(Modal.Model.Error(error.throwable))
                return@initialize
            }
        )
    }
    @Throws(IllegalStateException::class)
    fun setDelegate(delegate: ModalDelegate) {
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

            override fun onConnectionStateChange(state: Sign.Model.ConnectionState) {
                delegate.onConnectionStateChange(state.toModal())
            }

            override fun onError(error: Sign.Model.Error) {
                delegate.onError(error.toModal())
            }
        }
        SignClient.setDappDelegate(signDelegate)
    }

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
    fun getActiveSessionByTopic(topic: String) = SignClient.getActiveSessionByTopic(topic)?.toModal()

}
