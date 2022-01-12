package com.walletconnect.sample.wallet

import com.walletconnect.walletconnectv2.client.SettledSession

interface SessionActionListener {
    fun onDisconnect(session: SettledSession)
    fun onUpdate(session: SettledSession)
    fun onUpgrade(session: SettledSession)
    fun onPing(session: SettledSession)
    fun onSessionsDetails(session: SettledSession)
}