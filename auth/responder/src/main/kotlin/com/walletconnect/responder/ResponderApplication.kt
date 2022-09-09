package com.walletconnect.responder

import android.app.Application
import android.util.Log
import com.walletconnect.android.RelayClient
import com.walletconnect.android.connection.ConnectionType
import com.walletconnect.auth.client.Auth
import com.walletconnect.auth.client.AuthClient
import com.walletconnect.responder.domain.ISSUER
import com.walletconnect.sample_common.BuildConfig
import com.walletconnect.sample_common.WALLET_CONNECT_PROD_RELAY_URL
import com.walletconnect.sample_common.tag

class ResponderApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val serverUri = "wss://$WALLET_CONNECT_PROD_RELAY_URL?projectId=${BuildConfig.PROJECT_ID}"
        RelayClient.initialize(relayServerUrl = serverUri, connectionType = ConnectionType.AUTOMATIC, application = this)

        AuthClient.initialize(
            init = Auth.Params.Init(
                relay = RelayClient,
                appMetaData = Auth.Model.AppMetaData(
                    name = "Kotlin.Responder",
                    description = "Kotlin AuthSDK Responder Implementation",
                    url = "kotlin.responder.walletconnect.com",
                    icons = listOf("https://raw.githubusercontent.com/WalletConnect/walletconnect-assets/master/Logo/Gradient/Logo.png"),
                    redirect = "kotlin-responder-wc:/request"
                ),
                iss = ISSUER
            )
        ) { error ->
            Log.e(tag(this), error.throwable.stackTraceToString())
        }
    }
}