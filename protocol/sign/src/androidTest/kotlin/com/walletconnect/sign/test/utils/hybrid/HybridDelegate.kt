package com.walletconnect.sign.test.utils.hybrid

import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.client.SignClient
import com.walletconnect.sign.test.utils.globalOnError
import timber.log.Timber

open class HybridAppWalletDelegate : SignClient.WalletDelegate {
    override fun onSessionProposal(sessionProposal: Sign.Model.SessionProposal, verifyContext: Sign.Model.VerifyContext) {}
    override fun onSessionAuthenticate(sessionAuthenticate: Sign.Model.SessionAuthenticate, verifyContext: Sign.Model.VerifyContext) {}

    override fun onSessionRequest(sessionRequest: Sign.Model.SessionRequest, verifyContext: Sign.Model.VerifyContext) {}
    override fun onSessionDelete(deletedSession: Sign.Model.DeletedSession) {}
    override fun onSessionExtend(session: Sign.Model.Session) {}

    override fun onSessionSettleResponse(settleSessionResponse: Sign.Model.SettledSessionResponse) {}
    override fun onSessionUpdateResponse(sessionUpdateResponse: Sign.Model.SessionUpdateResponse) {}
    override fun onProposalExpired(proposal: Sign.Model.ExpiredProposal) {}
    override fun onRequestExpired(request: Sign.Model.ExpiredRequest) {}
    override fun onConnectionStateChange(state: Sign.Model.ConnectionState) {
        Timber.d("HybridWallet: onConnectionStateChange: $state")
    }

    override fun onError(error: Sign.Model.Error) {
        globalOnError(error)
    }
}

open class HybridAppDappDelegate : SignClient.DappDelegate {
    override fun onSessionRejected(rejectedSession: Sign.Model.RejectedSession) {}
    override fun onSessionUpdate(updatedSession: Sign.Model.UpdatedSession) {}
    override fun onSessionEvent(sessionEvent: Sign.Model.SessionEvent) {}
    override fun onSessionExtend(session: Sign.Model.Session) {}
    override fun onSessionDelete(deletedSession: Sign.Model.DeletedSession) {}
    override fun onSessionRequestResponse(response: Sign.Model.SessionRequestResponse) {}
    override fun onSessionAuthenticateResponse(sessionAuthenticateResponse: Sign.Model.SessionAuthenticateResponse) {}
    override fun onProposalExpired(proposal: Sign.Model.ExpiredProposal) {}
    override fun onRequestExpired(request: Sign.Model.ExpiredRequest) {}
    override fun onSessionApproved(approvedSession: Sign.Model.ApprovedSession) {}
    override fun onConnectionStateChange(state: Sign.Model.ConnectionState) {
        Timber.d("HybridDapp: onConnectionStateChange: $state")
    }

    override fun onError(error: Sign.Model.Error) {
        globalOnError(error)
    }
}