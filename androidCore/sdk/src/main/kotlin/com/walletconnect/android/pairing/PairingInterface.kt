package com.walletconnect.android.pairing

import com.walletconnect.android.Core

interface PairingInterface {
    fun pair(pair: Core.Params.Pair, onError: (Core.Model.Error) -> Unit)
    fun ping(ping: Core.Params.Ping, sessionPing: Core.Listeners.SessionPing? = null)
}