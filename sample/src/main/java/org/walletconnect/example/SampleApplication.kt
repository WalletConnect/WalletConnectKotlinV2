package org.walletconnect.example

import android.app.Application
import org.walletconnect.walletconnectv2.WalletConnectClient
import org.walletconnect.walletconnectv2.client.ClientTypes
import org.walletconnect.walletconnectv2.common.AppMetaData

class SampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        val initParams = ClientTypes.InitialParams(
            application = this,
            hostName = "relay.walletconnect.org",
            metadata = AppMetaData(
                name = "Kotlin Wallet",
                description = "Wallet description",
                url = "example.wallet",
                icons = listOf("https://gblobscdn.gitbook.com/spaces%2F-LJJeCjcLrr53DcT1Ml7%2Favatar.png?alt=media")
            )
        )

        WalletConnectClient.initialize(initParams)
    }
}