package com.walletconnect.sign.test.utils

import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.client.SignClient
import timber.log.Timber

open class WalletDelegate : SignClient.WalletDelegate {
    override fun onSessionProposal(sessionProposal: Sign.Model.SessionProposal, verifyContext: Sign.Model.VerifyContext) {}
    override fun onSessionRequest(sessionRequest: Sign.Model.SessionRequest, verifyContext: Sign.Model.VerifyContext) {}
    override fun onSessionDelete(deletedSession: Sign.Model.DeletedSession) {}
    override fun onSessionSettleResponse(settleSessionResponse: Sign.Model.SettledSessionResponse) {}
    override fun onSessionUpdateResponse(sessionUpdateResponse: Sign.Model.SessionUpdateResponse) {}
    override fun onConnectionStateChange(state: Sign.Model.ConnectionState) {
        Timber.d("onConnectionStateChange: $state")
    }

    override fun onError(error: Sign.Model.Error) {
        globalOnError(error)
    }
}

open class AutoApproveSessionWalletDelegate : WalletDelegate() {
    override fun onSessionProposal(sessionProposal: Sign.Model.SessionProposal, verifyContext: Sign.Model.VerifyContext) {
        sessionProposal.approveOnSessionProposal()
    }
}


internal fun Sign.Model.SessionProposal.approveOnSessionProposal() {
    Timber.d("walletDelegate: onSessionProposal")

    WalletSignClient.approveSession(Sign.Params.Approve(proposerPublicKey, sessionNamespaces), onSuccess = {}, onError = ::globalOnError)
    Timber.d("WalletClient: approveSession")
}


internal fun Sign.Model.SessionProposal.rejectOnSessionProposal() {
    Timber.d("walletDelegate: onSessionProposal")

    WalletSignClient.rejectSession(Sign.Params.Reject(proposerPublicKey, "test reason"), onSuccess = {}, onError = ::globalOnError)
    Timber.d("WalletClient: rejectSession")
}
