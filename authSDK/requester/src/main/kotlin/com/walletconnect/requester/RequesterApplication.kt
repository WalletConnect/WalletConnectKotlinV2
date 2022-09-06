package com.walletconnect.requester

import android.app.Application
import android.util.Log
import com.walletconnect.android.api.ConnectionType
import com.walletconnect.android.api.RelayClient
import com.walletconnect.auth.client.Auth
import com.walletconnect.auth.client.AuthClient
import com.walletconnect.sample_common.BuildConfig
import com.walletconnect.sample_common.WALLET_CONNECT_PROD_RELAY_URL
import com.walletconnect.sample_common.tag

class RequesterApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val serverUri = "wss://$WALLET_CONNECT_PROD_RELAY_URL?projectId=${BuildConfig.PROJECT_ID}"
        val relayClient = RelayClient(relayServerUrl = serverUri, connectionType = ConnectionType.AUTOMATIC, application = this)

        AuthClient.initialize(
            init = Auth.Params.Init(
                relay = relayClient,
                appMetaData = Auth.Model.AppMetaData(
                    name = "Kotlin.Requester",
                    description = "Kotlin AuthSDK Requester Implementation",
                    url = "kotlin.requester.walletconnect.com",
                    icons = listOf("https://raw.githubusercontent.com/WalletConnect/walletconnect-assets/master/Icon/Gradient/Icon.png"),
                    redirect = "kotlin-requester-wc:/request"
                ),
                iss = null
            )
        ) { error ->
            Log.e(tag(this), error.throwable.stackTraceToString())
        }
    }
}