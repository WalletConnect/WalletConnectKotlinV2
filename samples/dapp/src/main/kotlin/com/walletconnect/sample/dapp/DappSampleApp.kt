package com.walletconnect.sample.dapp

import android.app.Application
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.walletconnect.android.Core
import com.walletconnect.android.CoreClient
import com.walletconnect.android.relay.ConnectionType
import com.walletconnect.modal.client.Modal
import com.walletconnect.modal.client.WalletConnectModal
import com.walletconnect.push.common.Push
import com.walletconnect.push.dapp.client.PushDappClient
import com.walletconnect.sample_common.BuildConfig
import com.walletconnect.sample_common.WALLET_CONNECT_PROD_RELAY_URL
import com.walletconnect.sample_common.tag
import timber.log.Timber

class DappSampleApp : Application() {

    override fun onCreate() {
        super.onCreate()

        val serverUri = "wss://$WALLET_CONNECT_PROD_RELAY_URL?projectId=${BuildConfig.PROJECT_ID}"
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

        PushDappClient.initialize(Push.Dapp.Params.Init(CoreClient, null)) { error ->
            Timber.e(tag(this), error.throwable.stackTraceToString())
        }

        WalletConnectModal.initialize(
            Modal.Params.Init(
                core = CoreClient,
                excludedWalletIds = listOf("c57ca95b47569778a828d19178114f4db188b89b763c899ba0be274e97267d96"),
                recommendedWalletsIds = listOf("20459438007b75f4f4acb98bf29aa3b800550309646d375da5fd4aac6c2a2c66", "47bb07617af518642f3413a201ec5859faa63acb1dd175ca95085d35d38afb83")
            )
        ) { error ->
            Timber.e(tag(this), error.throwable.stackTraceToString())
        }
    }
}
