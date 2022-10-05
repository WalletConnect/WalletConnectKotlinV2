package com.walletconnect.responder

import android.app.Application
import android.util.Log
import com.walletconnect.android.Core
import com.walletconnect.android.CoreClient
import com.walletconnect.android.connection.ConnectionType
import com.walletconnect.android.relay.RelayClient
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
        CoreClient.initialize(Core.Model.AppMetaData("", "", serverUrl, emptyList(), null), "", ConnectionType.AUTOMATIC, this)

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