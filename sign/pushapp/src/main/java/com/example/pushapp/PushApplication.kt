package com.example.pushapp

import android.app.Application
import android.util.Log
import com.walletconnect.android.Core
import com.walletconnect.android.CoreClient
import com.walletconnect.android.relay.ConnectionType
import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.client.SignClient

class PushApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        //TODO: register at https://walletconnect.com/register to get a project ID
        val serverUri = "wss://relay.walletconnect.com?projectId=a7f155fbc59c18b6ad4fb5650067dd41"
        CoreClient.initialize(relayServerUrl = serverUri, connectionType = ConnectionType.AUTOMATIC, application = this,
            metaData = Core.Model.AppMetaData(
                name = "Kotlin Wallet",
                description = "Wallet description",
                url = "example.wallet",
                icons = listOf("https://gblobscdn.gitbook.com/spaces%2F-LJJeCjcLrr53DcT1Ml7%2Favatar.png?alt=media"),
                redirect = "kotlin-wallet-wc:/request",
            ))

        val initParams = Sign.Params.Init(core = CoreClient)

        SignClient.initialize(initParams) { error ->
            Log.e("kobe", error.throwable.stackTraceToString())
        }
    }
}