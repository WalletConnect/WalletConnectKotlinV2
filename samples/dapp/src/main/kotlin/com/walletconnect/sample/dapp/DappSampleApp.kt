package com.walletconnect.sample.dapp

import android.app.Application
import com.walletconnect.android.Core
import com.walletconnect.android.CoreClient
import com.walletconnect.android.relay.ConnectionType
import com.walletconnect.push.common.Push
import com.walletconnect.push.dapp.client.PushDappClient
import com.walletconnect.sample.dapp.web3modal.di.web3ModalModule
import com.walletconnect.sample_common.BuildConfig
import com.walletconnect.sample_common.WALLET_CONNECT_PROD_RELAY_URL
import com.walletconnect.sample_common.tag
import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.client.SignClient
import org.koin.core.context.startKoin
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
            redirect = "kotlin-dapp-wc:/request"
        )

        CoreClient.initialize(
            relayServerUrl = serverUri,
            connectionType = ConnectionType.AUTOMATIC,
            application = this,
            metaData = appMetaData,
        ) {
            Timber.e(tag(this), it.throwable.stackTraceToString())
        }

        val initParams = Sign.Params.Init(core = CoreClient)

        SignClient.initialize(initParams) { error ->
            Timber.e(tag(this), error.throwable.stackTraceToString())
        }

        PushDappClient.initialize(Push.Dapp.Params.Init(CoreClient, null)) { error ->
            Timber.e(tag(this), error.throwable.stackTraceToString())
        }

        startKoin {
            modules(web3ModalModule())
        }
    }
}
