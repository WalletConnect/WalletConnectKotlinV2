package com.walletconnect.sample.wallet

import com.walletconnect.walletconnectv2.client.WalletConnect

interface SessionActionListener {
    fun onDisconnect(session: WalletConnect.Model.Session)
    fun onUpdateAccounts(session: WalletConnect.Model.Session)
    fun onUpdateMethods(session: WalletConnect.Model.Session)
    fun onPing(session: WalletConnect.Model.Session)
    fun onSessionsDetails(session: WalletConnect.Model.Session)
}