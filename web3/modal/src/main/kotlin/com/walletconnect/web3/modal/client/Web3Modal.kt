package com.walletconnect.web3.modal.client

import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.auth.client.Auth
import com.walletconnect.auth.client.AuthClient
import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.client.SignClient
import com.walletconnect.web3.modal.di.web3ModalModule
import com.walletconnect.web3.modal.domain.delegate.Web3ModalDelegate

object Web3Modal {
    private var isClientInitialized = false

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

    // Todo set theme here maybe?
    // add exclude/recommended wallets
    fun initialize(
        init: Modal.Params.Init,
        onError: (Modal.Model.Error) -> Unit
    ) {
        SignClient.initialize(
            init = Sign.Params.Init(init.core),
            onError = { error -> onError(Modal.Model.Error(error.throwable)) }
        )
        AuthClient.initialize(
            params = Auth.Params.Init(init.core),
            onError = { error -> onError(Modal.Model.Error(error.throwable)) }
        )

        runCatching {
            wcKoinApp.modules(
                web3ModalModule()
            )
            setDelegate(Web3ModalDelegate)
            isClientInitialized = true
        }.onFailure { error -> onError(Modal.Model.Error(error)) }
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

    internal fun connect(
        connect: Modal.Params.Connect,
        onSuccess: () -> Unit,
        onError: (Sign.Model.Error) -> Unit
    ) {
        SignClient.connect(
            connect.toSign(),
            onSuccess,
            onError
        )
    }
}
