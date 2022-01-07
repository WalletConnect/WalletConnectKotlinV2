package com.walletconnect.walletconnectv2.client.presentation

import com.walletconnect.walletconnectv2.client.model.WalletConnectClientModel

interface WalletConnectClientListener {
    fun onSessionProposal(sessionProposal: WalletConnectClientModel.SessionProposal)
    fun onSessionRequest(sessionRequest: WalletConnectClientModel.SessionRequest)
    fun onSessionDelete(deletedSession: WalletConnectClientModel.DeletedSession)
    fun onSessionNotification(sessionNotification: WalletConnectClientModel.SessionNotification)
}

sealed interface WalletConnectClientListeners {

    fun onError(error: Throwable)

    interface Pairing : WalletConnectClientListeners {
        fun onSuccess(settledPairing: WalletConnectClientModel.SettledPairing)
    }

    interface SessionReject : WalletConnectClientListeners {
        fun onSuccess(rejectedSession: WalletConnectClientModel.RejectedSession)
    }

    interface SessionDelete : WalletConnectClientListeners {
        fun onSuccess(deletedSession: WalletConnectClientModel.DeletedSession)
    }

    interface SessionApprove : WalletConnectClientListeners {
        fun onSuccess(settledSession: WalletConnectClientModel.SettledSession)
    }

    interface SessionPayload : WalletConnectClientListeners

    interface SessionUpdate : WalletConnectClientListeners {
        fun onSuccess(updatedSession: WalletConnectClientModel.UpdatedSession)
    }

    interface SessionUpgrade : WalletConnectClientListeners {
        fun onSuccess(upgradedSession: WalletConnectClientModel.UpgradedSession)
    }

    interface SessionPing : WalletConnectClientListeners {
        fun onSuccess(topic: String)
    }

    interface Notification : WalletConnectClientListeners {
        fun onSuccess(topic: String)
    }
}