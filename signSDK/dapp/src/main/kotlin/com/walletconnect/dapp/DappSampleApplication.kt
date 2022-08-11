package com.walletconnect.dapp

import android.app.Application
import android.util.Log
import com.walletconnect.sample_common.BuildConfig
import com.walletconnect.sample_common.WALLET_CONNECT_PROD_RELAY_URL
import com.walletconnect.sample_common.tag
import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.client.SignClient

class DappSampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Sample of how to use parts of a URI to initialize the WalletConnect SDK
//        val initParts = WalletConnect.Params.Init(
//            application = this,
//            useTls = true,
//            hostName = WALLET_CONNECT_PROD_RELAY_URL,
//            projectId = PROJECT_ID,     //TODO: register at https://walletconnect.com/register to get a project ID
//            metadata = WalletConnect.Model.AppMetaData(
//                name = "Kotlin Dapp",
//                description = "Dapp description",
//                url = "example.dapp",
//                icons = listOf("https://gblobscdn.gitbook.com/spaces%2F-LJJeCjcLrr53DcT1Ml7%2Favatar.png?alt=media")
//            )
//        )

        // Sample of how to use a URI to initialize the WalletConnect SDK
        val initString = Sign.Params.Init(
            application = this,
            //TODO: register at https://walletconnect.com/register to get a project ID
            relayServerUrl = "wss://$WALLET_CONNECT_PROD_RELAY_URL?projectId=${BuildConfig.PROJECT_ID}",
            metadata = Sign.Model.AppMetaData(
                name = "Kotlin Dapp",
                description = "Dapp description",
                url = "example.dapp",
                icons = listOf("https://gblobscdn.gitbook.com/spaces%2F-LJJeCjcLrr53DcT1Ml7%2Favatar.png?alt=media"),
                redirect = "kotlin-dapp-wc:/request"
            )
        )

        SignClient.initialize(initString) { error ->
            Log.e(tag(this), error.throwable.stackTraceToString())
        }
    }
}