package com.walletconnect.sample

import android.app.Application
import com.walletconnect.walletconnectv2.client.WalletConnect
import com.walletconnect.walletconnectv2.client.WalletConnectClient

class SampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        val init = WalletConnect.Params.Init(
            application = this,
            metadata = WalletConnect.Model.AppMetaData(
                name = "Kotlin Wallet",
                description = "Wallet description",
                url = "example.wallet",
                icons = listOf("https://gblobscdn.gitbook.com/spaces%2F-LJJeCjcLrr53DcT1Ml7%2Favatar.png?alt=media")
            )
        )

        WalletConnectClient.initialize(init)
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)

        WalletConnectClient.shutdown()
    }
}