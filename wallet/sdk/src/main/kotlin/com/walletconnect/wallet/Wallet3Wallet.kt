package com.walletconnect.wallet

import com.walletconnect.auth.client.Auth
import com.walletconnect.auth.client.AuthClient
import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.client.SignClient

object Wallet3Wallet {

    interface WalletDelegate {
        fun onSessionProposal(sessionProposal: Wallet.Model.SessionProposal)
        fun onSessionRequest(sessionRequest: Wallet.Model.SessionRequest)
        fun onSessionDelete(deletedSession: Wallet.Model.SessionDelete)
        fun onAuthRequest(authRequest: Wallet.Model.AuthRequest)

        //Responses
        fun onSessionSettleResponse(settleSessionResponse: Wallet.Model.SettledSessionResponse)
        fun onSessionUpdateResponse(sessionUpdateResponse: Wallet.Model.SessionUpdateResponse)

        //Utils
        fun onConnectionStateChange(state: Wallet.Model.ConnectionState)
        fun onError(error: Wallet.Model.Error)
    }

    @Throws(IllegalStateException::class)
    fun setWalletDelegate(delegate: WalletDelegate) {

        val signWalletDelegate = object : SignClient.WalletDelegate {
            override fun onSessionProposal(sessionProposal: Sign.Model.SessionProposal) {
                delegate.onSessionProposal(sessionProposal.toWallet())
            }

            override fun onSessionRequest(sessionRequest: Sign.Model.SessionRequest) {
                delegate.onSessionRequest(sessionRequest.toWallet())
            }

            override fun onSessionDelete(deletedSession: Sign.Model.DeletedSession) {
                delegate.onSessionDelete(deletedSession.toWallet())
            }

            override fun onSessionSettleResponse(settleSessionResponse: Sign.Model.SettledSessionResponse) {
                delegate.onSessionSettleResponse(settleSessionResponse.toWallet())
            }

            override fun onSessionUpdateResponse(sessionUpdateResponse: Sign.Model.SessionUpdateResponse) {
                delegate.onSessionUpdateResponse(sessionUpdateResponse.toWallet())
            }

            override fun onConnectionStateChange(state: Sign.Model.ConnectionState) {
                delegate.onConnectionStateChange(Wallet.Model.ConnectionState(state.isAvailable))
            }

            override fun onError(error: Sign.Model.Error) {
                delegate.onError(Wallet.Model.Error(error.throwable))
            }
        }

        val authWalletDelegate = object : AuthClient.ResponderDelegate {
            override fun onAuthRequest(authRequest: Auth.Event.AuthRequest) {
                delegate.onAuthRequest(authRequest.toWallet())
            }

            override fun onConnectionStateChange(connectionStateChange: Auth.Event.ConnectionStateChange) {
                //ignore
            }

            override fun onError(error: Auth.Event.Error) {
                delegate.onError(Wallet.Model.Error(error.error.throwable))
            }

        }

        SignClient.setWalletDelegate(signWalletDelegate)
        AuthClient.setResponderDelegate(authWalletDelegate)
    }

    @Throws(IllegalStateException::class)
    fun initialize(init: Wallet.Params.Init, onError: (Wallet.Model.Error) -> Unit) {
        SignClient.initialize(Sign.Params.Init(init.core)) { error -> onError(Wallet.Model.Error(error.throwable)) }
        AuthClient.initialize(Auth.Params.Init(init.core)) { error -> onError(Wallet.Model.Error(error.throwable)) }
    }

    @Throws(IllegalStateException::class)
    fun approveSession(params: Wallet.Params.SessionApprove, onError: (Wallet.Model.Error) -> Unit) {
        val signParams = Sign.Params.Approve(params.proposerPublicKey, params.namespaces.toSign(), params.relayProtocol)
        SignClient.approveSession(signParams) { error -> onError(Wallet.Model.Error(error.throwable)) }
    }

    @Throws(IllegalStateException::class)
    fun rejectSession(params: Wallet.Params.SessionReject, onError: (Wallet.Model.Error) -> Unit) {
        val signParams = Sign.Params.Reject(params.proposerPublicKey, params.reason)
        SignClient.rejectSession(signParams) { error -> onError(Wallet.Model.Error(error.throwable)) }
    }

    @Throws(IllegalStateException::class)
    fun updateSession(params: Wallet.Params.SessionUpdate, onError: (Wallet.Model.Error) -> Unit) {
        val signParams = Sign.Params.Update(params.sessionTopic, params.namespaces.toSign())
        SignClient.update(signParams) { error -> onError(Wallet.Model.Error(error.throwable)) }
    }

    @Throws(IllegalStateException::class)
    fun extendSession(params: Wallet.Params.SessionExtend, onError: (Wallet.Model.Error) -> Unit) {
        val signParams = Sign.Params.Extend(params.topic)
        SignClient.extend(signParams) { error -> onError(Wallet.Model.Error(error.throwable)) }
    }

    @Throws(IllegalStateException::class)
    fun respondSessionRequest(params: Wallet.Params.SessionRequestResponse, onError: (Wallet.Model.Error) -> Unit) {
        val signParams = Sign.Params.Response(params.sessionTopic, params.jsonRpcResponse.toSign())
        SignClient.respond(signParams) { error -> onError(Wallet.Model.Error(error.throwable)) }
    }

    @Throws(IllegalStateException::class)
    fun emitSessionEvent(params: Wallet.Params.SessionEmit, onError: (Wallet.Model.Error) -> Unit) {
        val signParams = Sign.Params.Emit(params.topic, params.event.toSign(), params.chainId)
        SignClient.emit(signParams) { error -> onError(Wallet.Model.Error(error.throwable)) }
    }

    @Throws(IllegalStateException::class)
    fun disconnectSession(params: Wallet.Params.SessionDisconnect, onError: (Wallet.Model.Error) -> Unit) {
        val signParams = Sign.Params.Disconnect(params.sessionTopic)
        SignClient.disconnect(signParams) { error -> onError(Wallet.Model.Error(error.throwable)) }
    }

    @Throws(IllegalStateException::class)
    fun formatMessage(params: Wallet.Params.FormatMessage): String? {
        val authParams = Auth.Params.FormatMessage(params.payloadParams.toSign(), params.issuer)
        return AuthClient.formatMessage(authParams)
    }

    @Throws(IllegalStateException::class)
    fun respondAuthRequest(params: Wallet.Params.AuthRequestResponse, onError: (Wallet.Model.Error) -> Unit) {
        AuthClient.respond(params.toAuth()) { error -> onError(Wallet.Model.Error(error.throwable)) }
    }

    @Throws(IllegalStateException::class)
    fun getListOfActiveSessions(): List<Wallet.Model.Session> {
        return SignClient.getListOfActiveSessions().map(Sign.Model.Session::toWallet)
    }

    @Throws(IllegalStateException::class)
    fun getActiveSessionByTopic(topic: String): Wallet.Model.Session? {
        return SignClient.getActiveSessionByTopic(topic)?.toWallet()
    }

    @Throws(IllegalStateException::class)
    fun getPendingSessionRequests(topic: String): List<Wallet.Model.PendingSessionRequest> {
        return SignClient.getPendingRequests(topic).mapToPendingRequests()
    }

    @Throws(IllegalStateException::class)
    fun getPendingAuthRequests(topic: String): List<Wallet.Model.PendingAuthRequest> {
        return AuthClient.getPendingRequest().toWallet()
    }
}