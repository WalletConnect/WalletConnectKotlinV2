package org.walletconnect.walletconnectv2.client

interface WalletConnectClientListener {
    fun onSessionProposal(proposal: WalletConnectClientData.SessionProposal)
    fun onSessionRequest(request: WalletConnectClientData.SessionRequest)
    fun onSessionDelete(topic: String, reason: String)
}

sealed interface WalletConnectClientListeners {

    fun onError(error: Throwable)

    interface Pairing : WalletConnectClientListeners {
        fun onSuccess(settledPairing: WalletConnectClientData.SettledPairing)
    }

    interface SessionReject : WalletConnectClientListeners {
        fun onSuccess(rejectedSession: WalletConnectClientData.RejectedSession)
    }

    interface SessionDelete : WalletConnectClientListeners {
        fun onSuccess(deletedSession: WalletConnectClientData.DeletedSession)
    }

    interface SessionApprove : WalletConnectClientListeners {
        fun onSuccess(settledSession: WalletConnectClientData.SettledSession)
    }
}