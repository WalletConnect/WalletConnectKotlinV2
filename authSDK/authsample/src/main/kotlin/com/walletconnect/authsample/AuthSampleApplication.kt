package com.walletconnect.authsample

import android.app.Application
import android.util.Log
import com.walletconnect.auth.client.Auth
import com.walletconnect.auth.client.AuthClient

class AuthSampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        val metadata = Auth.Model.AppMetaData(
            name = "Kotlin Wallet",
            description = "Wallet description",
            url = "example.wallet",
            icons = listOf("https://gblobscdn.gitbook.com/spaces%2F-LJJeCjcLrr53DcT1Ml7%2Favatar.png?alt=media"),
            redirect = "kotlin-wallet-wc:/request"
        )
        val init = Auth.Params.Init(application = this, metadata, null)
        AuthClient.initialize(init) {
            Log.e("kobe", it.throwable.stackTraceToString())
        }
    }
}