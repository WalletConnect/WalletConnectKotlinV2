package com.walletconnect.sign.test.utils.wallet

import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.client.SignClient
import com.walletconnect.sign.test.utils.globalOnError
import com.walletconnect.sign.test.utils.sessionNamespaces
import timber.log.Timber

open class WalletDelegate : SignClient.WalletDelegate {
    override fun onSessionProposal(sessionProposal: Sign.Model.SessionProposal, verifyContext: Sign.Model.VerifyContext) {}

    override val onSessionAuthenticate: ((Sign.Model.SessionAuthenticate, Sign.Model.VerifyContext) -> Unit)
        get() = { _, _ -> }

    override fun onSessionRequest(sessionRequest: Sign.Model.SessionRequest, verifyContext: Sign.Model.VerifyContext) {}
    override fun onSessionDelete(deletedSession: Sign.Model.DeletedSession) {}
    override fun onSessionExtend(session: Sign.Model.Session) {}

    override fun onSessionSettleResponse(settleSessionResponse: Sign.Model.SettledSessionResponse) {}
    override fun onSessionUpdateResponse(sessionUpdateResponse: Sign.Model.SessionUpdateResponse) {}
    override fun onProposalExpired(proposal: Sign.Model.ExpiredProposal) {}
    override fun onRequestExpired(request: Sign.Model.ExpiredRequest) {}
    override fun onConnectionStateChange(state: Sign.Model.ConnectionState) {
        Timber.d("Wallet: onConnectionStateChange: $state")
    }

    override fun onError(error: Sign.Model.Error) {
        globalOnError(error)
    }
}

open class AutoApproveSessionWalletDelegate : WalletDelegate() {
    override fun onSessionProposal(sessionProposal: Sign.Model.SessionProposal, verifyContext: Sign.Model.VerifyContext) {
        sessionProposal.approveOnSessionProposal()
    }

    override val onSessionAuthenticate: ((Sign.Model.SessionAuthenticate, Sign.Model.VerifyContext) -> Unit)
        get() = { _, _ -> }
}


internal fun Sign.Model.SessionProposal.approveOnSessionProposal() {
    Timber.d("walletDelegate: onSessionProposal: $this")

    WalletSignClient.approveSession(Sign.Params.Approve(proposerPublicKey, sessionNamespaces),
        onSuccess = {}, onError = { globalOnError(it) }
    )
    Timber.d("WalletClient: approveSession")
}


internal fun Sign.Model.SessionProposal.rejectOnSessionProposal() {
    Timber.d("walletDelegate: onSessionProposal")

    WalletSignClient.rejectSession(Sign.Params.Reject(proposerPublicKey, "test reason"), onSuccess = {}, onError = ::globalOnError)
    Timber.d("WalletClient: rejectSession")
}
