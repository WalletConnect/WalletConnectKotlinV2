package com.walletconnect.wallet

import android.app.Application
import com.walletconnect.sample_common.PROJECT_ID
import com.walletconnect.sample_common.WALLET_CONNECT_PROD_RELAY_URL
import com.walletconnect.walletconnectv2.client.WalletConnect
import com.walletconnect.walletconnectv2.client.WalletConnectClient

class WalletSampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Sample of how to use parts of a URI to initialize the WalletConnect SDK
//        val initParts = WalletConnect.Params.Init(
//            application = this,
//            useTls = true,
//            hostName = WALLET_CONNECT_PROD_RELAY_URL,
//            projectId = PROJECT_ID,     //TODO: register at https://walletconnect.com/register to get a project ID
//            isController = false,
//        metadata = WalletConnect.Model.AppMetaData(
//            name = "Kotlin Wallet",
//            description = "Wallet description",
//            url = "example.wallet",
//            icons = listOf("https://gblobscdn.gitbook.com/spaces%2F-LJJeCjcLrr53DcT1Ml7%2Favatar.png?alt=media")
//        )
//        )

        // Sample of how to use a URI to initialize the WalletConnect SDK
        val initString = WalletConnect.Params.Init(
            application = this,
            relayServerUrl = "wss://$WALLET_CONNECT_PROD_RELAY_URL?projectId=$PROJECT_ID",   //TODO: register at https://walletconnect.com/register to get a project ID
            metadata = WalletConnect.Model.AppMetaData(
                name = "Kotlin Wallet",
                description = "Wallet description",
                url = "example.wallet",
                icons = listOf("https://gblobscdn.gitbook.com/spaces%2F-LJJeCjcLrr53DcT1Ml7%2Favatar.png?alt=media")
            )
        )

        WalletConnectClient.initialize(initString)
    }
}