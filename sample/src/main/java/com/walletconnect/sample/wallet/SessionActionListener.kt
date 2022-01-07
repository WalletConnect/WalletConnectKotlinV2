package com.walletconnect.sample.wallet

import com.walletconnect.walletconnectv2.client.model.WalletConnectClientData

interface SessionActionListener {
    fun onDisconnect(session: WalletConnectClientData.SettledSession)
    fun onUpdate(session: WalletConnectClientData.SettledSession)
    fun onUpgrade(session: WalletConnectClientData.SettledSession)
    fun onPing(session: WalletConnectClientData.SettledSession)
    fun onSessionsDetails(session: WalletConnectClientData.SettledSession)
}