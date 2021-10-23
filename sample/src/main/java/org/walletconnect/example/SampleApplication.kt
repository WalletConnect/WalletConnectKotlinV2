package org.walletconnect.example

import android.app.Application
import org.walletconnect.walletconnectv2.WalletConnectClient
import org.walletconnect.walletconnectv2.client.ClientTypes

class SampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        val initParams = ClientTypes.InitialParams(true, "relay.walletconnect.org", "", true, this)
        WalletConnectClient.initialize(initParams)
    }
}