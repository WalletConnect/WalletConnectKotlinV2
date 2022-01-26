package com.walletconnect.sample

import android.app.Application
import com.walletconnect.walletconnectv2.client.WalletConnect
import com.walletconnect.walletconnectv2.client.WalletConnectClient

class SampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // TODO: Move to Dapp example once separate workflow is added
        val init = WalletConnect.Params.Init(
            application = this,
            useTls = true,
            hostName = WALLET_CONNECT_URL,
            projectId = PROJECT_ID,
            isController = false,
            metadata = WalletConnect.Model.AppMetaData(
                name = "Kotlin Dapp",
                description = "Dapp description",
                url = "example.dapp",
                icons = listOf("https://gblobscdn.gitbook.com/spaces%2F-LJJeCjcLrr53DcT1Ml7%2Favatar.png?alt=media")
            )
        )

        // TODO: Move to Wallet example once separate workflow is added
        val init2 = WalletConnect.Params.Init(
            application = this,
            relayServerUrl = "wss://$WALLET_CONNECT_URL/?projectId=$PROJECT_ID",
            isController = true,
            metadata = WalletConnect.Model.AppMetaData(
                name = "Kotlin Wallet",
                description = "Wallet description",
                url = "example.wallet",
                icons = listOf("https://gblobscdn.gitbook.com/spaces%2F-LJJeCjcLrr53DcT1Ml7%2Favatar.png?alt=media")
            )
        )

        WalletConnectClient.initialize(init2)
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)

        WalletConnectClient.shutdown()
    }

    private companion object {
        const val WALLET_CONNECT_URL = "relay.walletconnect.com"
        const val PROJECT_ID = "2ee94aca5d98e6c05c38bce02bee952a" //TODO: register at https://walletconnect.com/register to get a project ID
    }
}