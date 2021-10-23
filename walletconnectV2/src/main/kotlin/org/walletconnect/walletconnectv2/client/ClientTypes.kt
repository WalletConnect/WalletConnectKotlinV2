package org.walletconnect.walletconnectv2.client

import android.app.Application

sealed class ClientTypes {
    data class InitialParams(
        val useTls: Boolean,
        val hostName: String,
        val apiKey: String,
        val isController: Boolean,
        val application: Application
    ) : ClientTypes()

    data class PairParams(val uri: String) : ClientTypes()
}
