package com.walletconnect.sign.test.utils

import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.client.SignClient
import timber.log.Timber


open class WalletDelegate : SignClient.WalletDelegate {
    override fun onSessionRequest(sessionRequest: Sign.Model.SessionRequest) {}
    override fun onSessionDelete(deletedSession: Sign.Model.DeletedSession) {}
    override fun onSessionSettleResponse(settleSessionResponse: Sign.Model.SettledSessionResponse) {}
    override fun onSessionUpdateResponse(sessionUpdateResponse: Sign.Model.SessionUpdateResponse) {}
    override fun onSessionProposal(sessionProposal: Sign.Model.SessionProposal) {}
    override fun onConnectionStateChange(state: Sign.Model.ConnectionState) {
        Timber.d("onConnectionStateChange: $state")
    }
    override fun onError(error: Sign.Model.Error) {
        globalOnError(error)
    }
}

open class DappDelegate : SignClient.DappDelegate {
    override fun onSessionRejected(rejectedSession: Sign.Model.RejectedSession) {}
    override fun onSessionUpdate(updatedSession: Sign.Model.UpdatedSession) {}
    override fun onSessionEvent(sessionEvent: Sign.Model.SessionEvent) {}
    override fun onSessionExtend(session: Sign.Model.Session) {}
    override fun onSessionDelete(deletedSession: Sign.Model.DeletedSession) {}
    override fun onSessionRequestResponse(response: Sign.Model.SessionRequestResponse) {}
    override fun onSessionApproved(approvedSession: Sign.Model.ApprovedSession) {}
    override fun onConnectionStateChange(state: Sign.Model.ConnectionState) {
        Timber.d("onConnectionStateChange: $state")
    }
    override fun onError(error: Sign.Model.Error) {
        globalOnError(error)
    }
}