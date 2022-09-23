package com.walletconnect.android

import android.app.Application
import com.walletconnect.android.connection.ConnectionType
import com.walletconnect.android.pairing.PairingClient
import com.walletconnect.android.pairing.PairingInterface
import com.walletconnect.android.relay.RelayClient
import com.walletconnect.android.relay.RelayConnectionInterface

object CoreClient : RelayConnectionInterface by RelayClient, PairingInterface by PairingClient {
    fun initialize(relayServerUrl: String, connectionType: ConnectionType, application: Application) {
        RelayClient.initialize(relayServerUrl, connectionType, application)
    }
}