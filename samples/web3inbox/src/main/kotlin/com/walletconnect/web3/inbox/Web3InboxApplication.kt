package com.walletconnect.web3.inbox

import android.app.Application
//import com.google.firebase.crashlytics.ktx.crashlytics
//import com.google.firebase.ktx.Firebase
import com.walletconnect.android.Core
import com.walletconnect.android.CoreClient
import com.walletconnect.android.relay.ConnectionType
import com.walletconnect.web3.inbox.sample.BuildConfig

class Web3InboxApplication : Application() {
    override fun onCreate() {
        super.onCreate()

//        val projectId = BuildConfig.PROJECT_ID
//        val relayUrl = "relay.walletconnect.com"
//        val serverUrl = "wss://$relayUrl?projectId=${projectId}"
//        val appMetaData = Core.Model.AppMetaData(
//            name = "Kotlin.Web3Inbox",
//            description = "Kotlin Web3Inbox Implementation",
//            url = "kotlin.web3inbox.walletconnect.com",
//            icons = listOf("https://raw.githubusercontent.com/WalletConnect/walletconnect-assets/master/Icon/Gradient/Icon.png"),
//            redirect = null
//        )
//
//        CoreClient.initialize(
//            relayServerUrl = serverUrl,
//            connectionType = ConnectionType.AUTOMATIC,
//            application = this,
//            metaData = appMetaData
//        ) { error ->
//            Firebase.crashlytics.recordException(error.throwable)
//        }

//        Web3Inbox.initialize(Wallet.Params.Init(core = CoreClient)) { error ->
//            Firebase.crashlytics.recordException(error.throwable)
//        }
    }
}