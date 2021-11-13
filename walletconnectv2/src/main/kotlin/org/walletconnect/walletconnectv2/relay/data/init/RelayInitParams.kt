package org.walletconnect.walletconnectv2.relay.data.init

import android.app.Application

data class RelayInitParams(
    val useTls: Boolean,
    val hostName: String,
    val apiKey: String,
    val application: Application
)