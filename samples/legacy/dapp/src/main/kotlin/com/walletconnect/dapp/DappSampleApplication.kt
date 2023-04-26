package com.walletconnect.dapp

import android.app.Application
import android.util.Log
import com.walletconnect.android.Core
import com.walletconnect.android.CoreClient
import com.walletconnect.android.relay.ConnectionType
import com.walletconnect.push.common.Push
import com.walletconnect.push.dapp.client.PushDappClient
import com.walletconnect.sample_common.BuildConfig
import com.walletconnect.sample_common.WALLET_CONNECT_PROD_RELAY_URL
import com.walletconnect.sample_common.tag
import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.client.SignClient

class DappSampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        //TODO: register at https://walletconnect.com/register to get a project ID
        val serverUri = "wss://$WALLET_CONNECT_PROD_RELAY_URL?projectId=${BuildConfig.PROJECT_ID}"
        CoreClient.initialize(
            relayServerUrl = serverUri, connectionType = ConnectionType.AUTOMATIC, application = this, metaData = Core.Model.AppMetaData(
                name = "Kotlin Dapp",
                description = "Dapp description",
                url = "example.dapp",
                icons = listOf("https://gblobscdn.gitbook.com/spaces%2F-LJJeCjcLrr53DcT1Ml7%2Favatar.png?alt=media"),
                redirect = "kotlin-dapp-wc:/request"
            )
        ) { error -> Log.e(tag(this),error.throwable.stackTraceToString()) }

        val initParams = Sign.Params.Init(core = CoreClient)

        SignClient.initialize(initParams) { error ->
            Log.e(tag(this), error.throwable.stackTraceToString())
        }

        PushDappClient.initialize(Push.Dapp.Params.Init(CoreClient, null)) { error ->
            Log.e(tag(this), error.throwable.stackTraceToString())
        }
    }
}