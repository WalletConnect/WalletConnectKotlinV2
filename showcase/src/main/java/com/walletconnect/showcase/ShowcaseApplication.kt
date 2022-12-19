package com.walletconnect.showcase

import android.app.Application
import android.util.Log
import com.walletconnect.android.Core
import com.walletconnect.android.CoreClient
import com.walletconnect.android.relay.ConnectionType
import com.walletconnect.web3.wallet.client.Wallet
import com.walletconnect.web3.wallet.client.Web3Wallet

class ShowcaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val projectId = BuildConfig.PROJECT_ID
        val relayUrl = "relay.walletconnect.com"
        val serverUrl = "wss://$relayUrl?projectId=${projectId}"
        val appMetaData = Core.Model.AppMetaData(
            name = "Kotlin.Showcase",
            description = "Kotlin Showcase Implementation",
            url = "kotlin.showcase.walletconnect.com",
            icons = listOf("https://raw.githubusercontent.com/WalletConnect/walletconnect-assets/master/Icon/Gradient/Icon.png"),
            redirect = "kotlin-showcase:/request"
        )

        CoreClient.initialize(relayServerUrl = serverUrl, connectionType = ConnectionType.AUTOMATIC, application = this, metaData = appMetaData) { error ->
            Log.e("CoreClient", error.throwable.stackTraceToString())
        }

        Web3Wallet.initialize(Wallet.Params.Init(core = CoreClient)) { error ->
            Log.e("Web3Wallet", error.throwable.stackTraceToString())
        }
    }
}