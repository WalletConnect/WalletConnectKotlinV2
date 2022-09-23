package com.walletconnect.android.pairing

import com.walletconnect.android.Core

internal object PairingClient: PairingInterface {
    override fun pair(pair: Core.Params.Pair, onError: (Core.Model.Error) -> Unit) {

    }

    override fun ping(ping: Core.Params.Ping, sessionPing: Core.Listeners.SessionPing?) {

    }
}