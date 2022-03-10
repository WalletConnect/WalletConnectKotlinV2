package com.walletconnect.dapp

import android.app.Application
import android.content.Context
import com.walletconnect.dapp.ui.connect.chain_select.ChainSelectionFragment
import com.walletconnect.sample_common.WALLET_CONNECT_PROD_RELAY_URL
import com.walletconnect.walletconnectv2.client.WalletConnect
import com.walletconnect.walletconnectv2.client.WalletConnectClient

class DappSampleApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        // Sample of how to use parts of a URI to initialize the WalletConnect SDK
        val initParts = WalletConnect.Params.Init(
            application = this,
            useTls = true,
            hostName = WALLET_CONNECT_PROD_RELAY_URL,
            projectId = "",     //TODO: register at https://walletconnect.com/register to get a project ID
            isController = false,
            metadata = WalletConnect.Model.AppMetaData(
                name = "Kotlin Dapp",
                description = "Dapp description",
                url = "example.dapp",
                icons = listOf("https://gblobscdn.gitbook.com/spaces%2F-LJJeCjcLrr53DcT1Ml7%2Favatar.png?alt=media")
            )
        )

        // Sample of how to use a URI to initialize the WalletConnect SDK
        val initString = WalletConnect.Params.Init(
            application = this,
            relayServerUrl = "wss://$WALLET_CONNECT_PROD_RELAY_URL?projectId=",   //TODO: register at https://walletconnect.com/register to get a project ID
            isController = false,
            metadata = WalletConnect.Model.AppMetaData(
                name = "Kotlin Dapp",
                description = "Dapp description",
                url = "example.dapp",
                icons = listOf("https://gblobscdn.gitbook.com/spaces%2F-LJJeCjcLrr53DcT1Ml7%2Favatar.png?alt=media")
            )
        )

        WalletConnectClient.initialize(initString)
    }
}

inline fun <reified T: Any> tag(currentClass: T): String {
    return currentClass::class.java.canonicalName!!.substringAfterLast(".")
}