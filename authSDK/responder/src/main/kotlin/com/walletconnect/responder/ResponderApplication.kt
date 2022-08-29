package com.walletconnect.responder

import android.app.Application
import android.util.Log
import com.walletconnect.auth.client.Auth
import com.walletconnect.auth.client.AuthClient
import com.walletconnect.responder.domain.ISSUER
import com.walletconnect.sample_common.tag

class ResponderApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        AuthClient.initialize(
            init = Auth.Params.Init(
                application = this,
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
