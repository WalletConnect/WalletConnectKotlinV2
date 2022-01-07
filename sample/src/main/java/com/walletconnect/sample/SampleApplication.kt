package com.walletconnect.sample

import android.app.Application
import com.walletconnect.walletconnectv2.client.model.types.ClientTypes
import com.walletconnect.walletconnectv2.client.model.WalletConnectClientModel
import com.walletconnect.walletconnectv2.client.presentation.WalletConnectClient

class SampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        val initParams = ClientTypes.InitialParams(
            application = this,
            hostName = "relay.walletconnect.org",
            metadata = WalletConnectClientModel.AppMetaData(
                name = "Kotlin Wallet",
                description = "Wallet description",
                url = "example.wallet",
                icons = listOf("https://gblobscdn.gitbook.com/spaces%2F-LJJeCjcLrr53DcT1Ml7%2Favatar.png?alt=media")
            )
        )

        WalletConnectClient.initialize(initParams)
    }
}