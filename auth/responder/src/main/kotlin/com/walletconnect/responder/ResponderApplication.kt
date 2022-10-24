package com.walletconnect.responder

import android.app.Application
import android.util.Log
import com.walletconnect.android.Core
import com.walletconnect.android.CoreClient
import com.walletconnect.android.relay.ConnectionType
import com.walletconnect.auth.client.Auth
import com.walletconnect.auth.client.AuthClient
import com.walletconnect.responder.domain.ISSUER
import com.walletconnect.sample_common.BuildConfig
import com.walletconnect.sample_common.WALLET_CONNECT_PROD_RELAY_URL
import com.walletconnect.sample_common.tag

class ResponderApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val serverUrl = "wss://$WALLET_CONNECT_PROD_RELAY_URL?projectId=${BuildConfig.PROJECT_ID}"

        CoreClient.initialize(relayServerUrl = serverUrl, connectionType = ConnectionType.AUTOMATIC, application = this,
            metaData = Core.Model.AppMetaData(name = "Kotlin.Responder",
                description = "Kotlin AuthSDK Responder Implementation",
                url = "kotlin.responder.walletconnect.com",
                icons = listOf("https://raw.githubusercontent.com/WalletConnect/walletconnect-assets/master/Icon/Gradient/Icon.png"),
                redirect = "kotlin-responder-wc:/request")
        )

        AuthClient.initialize(
            init = Auth.Params.Init(
                core = CoreClient,
                iss = ISSUER
            )
        ) { error ->
            Log.e(tag(this), error.throwable.stackTraceToString())
        }
    }
}