package com.walletconnect.sample.wallet

import com.walletconnect.walletconnectv2.client.model.WalletConnectClientModel

interface SessionActionListener {
    fun onDisconnect(session: WalletConnectClientModel.SettledSession)
    fun onUpdate(session: WalletConnectClientModel.SettledSession)
    fun onUpgrade(session: WalletConnectClientModel.SettledSession)
    fun onPing(session: WalletConnectClientModel.SettledSession)
    fun onSessionsDetails(session: WalletConnectClientModel.SettledSession)
}