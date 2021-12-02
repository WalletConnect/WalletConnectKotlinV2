package org.walletconnect.walletconnectv2.client

interface WalletConnectClientListener {
    fun onSessionProposal(sessionProposal: WalletConnectClientData.SessionProposal)
    fun onSessionRequest(sessionRequest: WalletConnectClientData.SessionRequest)
    fun onSessionDelete(deletedSession: WalletConnectClientData.DeletedSession)
    fun onSessionNotification(sessionNotification: WalletConnectClientData.SessionNotification)
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

    interface SessionPayload : WalletConnectClientListeners {
        fun onSuccess(sessionPayloadResponse: WalletConnectClientData.Response)
    }

    interface SessionUpdate : WalletConnectClientListeners {
        fun onSuccess(updatedSession: WalletConnectClientData.UpdatedSession)
    }

    interface SessionUpgrade : WalletConnectClientListeners {
        fun onSuccess(upgradedSession: WalletConnectClientData.UpgradedSession)
    }

    interface SessionPing : WalletConnectClientListeners {
        fun onSuccess(topic: String)
    }

    interface Notification : WalletConnectClientListeners {
        fun onSuccess(topic: String)
    }
}