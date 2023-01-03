package com.walletconnect.wallet

import android.app.Application
import android.util.Log
import com.walletconnect.android.Core
import com.walletconnect.android.CoreClient
import com.walletconnect.android.relay.ConnectionType
import com.walletconnect.sample_common.BuildConfig
import com.walletconnect.sample_common.WALLET_CONNECT_PROD_RELAY_URL
import com.walletconnect.sample_common.tag
import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.client.SignClient

class WalletSampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        //TODO: register at https://walletconnect.com/register to get a project ID
        val serverUri = "wss://$WALLET_CONNECT_PROD_RELAY_URL?projectId=${BuildConfig.PROJECT_ID}"
        val metadata = Core.Model.AppMetaData(
            name = "Kotlin Wallet",
            description = "Wallet description",
            url = "example.wallet",
            icons = listOf("https://gblobscdn.gitbook.com/spaces%2F-LJJeCjcLrr53DcT1Ml7%2Favatar.png?alt=media"),
            redirect = "kotlin-wallet-wc:/request"
        )

        CoreClient.initialize(
            relayServerUrl = serverUri,
            connectionType = ConnectionType.AUTOMATIC,
            application = this,
            metaData = metadata
        ) { error -> Log.e(tag(this), error.throwable.stackTraceToString()) }

        val initParams = Sign.Params.Init(core = CoreClient)
        SignClient.initialize(initParams) { error ->
            Log.e(tag(this), error.throwable.stackTraceToString())
        }
    }
}