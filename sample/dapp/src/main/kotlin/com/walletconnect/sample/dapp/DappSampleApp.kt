package com.walletconnect.sample.dapp

import android.app.Application
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.walletconnect.android.Core
import com.walletconnect.android.CoreClient
import com.walletconnect.android.relay.ConnectionType
import com.walletconnect.sample.common.BuildConfig
import com.walletconnect.sample.common.RELAY_URL
import com.walletconnect.sample.common.tag
import com.walletconnect.wcmodal.client.Modal
import com.walletconnect.wcmodal.client.WalletConnectModal
import timber.log.Timber

class DappSampleApp : Application() {

    override fun onCreate() {
        super.onCreate()

        val serverUri = "wss://$RELAY_URL?projectId=${BuildConfig.PROJECT_ID}"
        val appMetaData = Core.Model.AppMetaData(
            name = "Kotlin Dapp",
            description = "Kotlin Dapp Implementation",
            url = "kotlin.dapp.walletconnect.com",
            icons = listOf("https://gblobscdn.gitbook.com/spaces%2F-LJJeCjcLrr53DcT1Ml7%2Favatar.png?alt=media"),
            redirect = "kotlin-dapp-wc://request"
        )

        CoreClient.initialize(
            relayServerUrl = serverUri,
            connectionType = ConnectionType.AUTOMATIC,
            application = this,
            metaData = appMetaData,
        ) {
            Firebase.crashlytics.recordException(it.throwable)
        }

        WalletConnectModal.initialize(
            Modal.Params.Init(core = CoreClient)
        ) { error ->
            Timber.e(tag(this), error.throwable.stackTraceToString())
        }
    }
}
