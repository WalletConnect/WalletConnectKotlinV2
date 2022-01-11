package com.walletconnect.walletconnectv2.client

interface Pairing : WalletConnectClient.Listeners {
    fun onSuccess(settledPairing: SettledPairing)
}

interface SessionReject : WalletConnectClient.Listeners {
    fun onSuccess(rejectedSession: RejectedSession)
}

interface SessionDelete : WalletConnectClient.Listeners {
    fun onSuccess(deletedSession: DeletedSession)
}

interface SessionApprove : WalletConnectClient.Listeners {
    fun onSuccess(settledSession: SettledSession)
}

interface SessionPayload : WalletConnectClient.Listeners

interface SessionUpdate : WalletConnectClient.Listeners {
    fun onSuccess(updatedSession: UpdatedSession)
}

interface SessionUpgrade : WalletConnectClient.Listeners {
    fun onSuccess(upgradedSession: UpgradedSession)
}

interface SessionPing : WalletConnectClient.Listeners {
    fun onSuccess(topic: String)
}

interface NotificationListener : WalletConnectClient.Listeners {
    fun onSuccess(topic: String)
}