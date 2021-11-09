package org.walletconnect.walletconnectv2.client

interface WalletConnectClientListener {
    fun onSessionProposal(proposal: SessionProposal)
    fun onSettledSession(session: SettledSession)
    fun onSessionRequest(request: SessionRequest)
    fun onSessionDelete(topic: String, reason: String)
}