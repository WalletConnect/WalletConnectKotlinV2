package com.walletconnect.sample.wallet

import com.walletconnect.walletconnectv2.client.WalletConnect

interface SessionActionListener {
    fun onDisconnect(session: WalletConnect.Model.SettledSession)
    fun onUpdate(session: WalletConnect.Model.SettledSession)
    fun onUpgrade(session: WalletConnect.Model.SettledSession)
    fun onPing(session: WalletConnect.Model.SettledSession)
    fun onSessionsDetails(session: WalletConnect.Model.SettledSession)
}