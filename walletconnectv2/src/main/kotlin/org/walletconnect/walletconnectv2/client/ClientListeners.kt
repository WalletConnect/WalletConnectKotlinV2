package org.walletconnect.walletconnectv2.client

interface WalletConnectClientListener {

    fun onSessionProposal(proposal: WalletConnectClientData.SessionProposal)

    fun onSettledSession(session: WalletConnectClientData.SettledSession)

    fun onSessionRequest(request: WalletConnectClientData.SessionRequest)

    fun onSessionDelete(topic: String, reason: String)
}