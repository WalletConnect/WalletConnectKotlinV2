package com.walletconnect.wallet

import com.walletconnect.auth.client.Auth
import com.walletconnect.auth.client.AuthClient
import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.client.SignClient
import com.walletconnect.sign.client.SignInterface

object WalletClient {

    interface WalletDelegate : SignInterface.WalletDelegate, AuthClient.RequesterDelegate

    @Throws(IllegalStateException::class)
    fun init(init: Wallet.Params.Init, onError: (Wallet.Model.Error) -> Unit) {
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