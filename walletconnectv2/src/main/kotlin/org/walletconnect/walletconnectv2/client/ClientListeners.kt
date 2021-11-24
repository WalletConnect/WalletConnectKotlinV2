package org.walletconnect.walletconnectv2.client

//Note: we should keep a global listener because when resubscribing the communication on topics should be ready just from the initialization, without any public method call
interface WalletConnectClientListener {
    fun onSessionProposal(sessionProposal: WalletConnectClientData.SessionProposal)
    fun onSessionRequest(sessionRequest: WalletConnectClientData.SessionRequest)
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

    interface SessionPayload : WalletConnectClientListeners {
        fun onSuccess(sessionPayloadResponse: WalletConnectClientData.Response)
    }

    interface SessionUpdate : WalletConnectClientListeners {
        fun onSuccess(updatedSession: WalletConnectClientData.UpdatedSession)
    }
}