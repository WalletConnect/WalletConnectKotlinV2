package com.walletconnect.wallet

import android.app.Application
import android.util.Log
import com.walletconnect.android.api.ConnectionType
import com.walletconnect.android.api.RelayClient
import com.walletconnect.foundation.network.data.service.RelayService
import com.walletconnect.sample_common.BuildConfig
import com.walletconnect.sample_common.WALLET_CONNECT_PROD_RELAY_URL
import com.walletconnect.sample_common.tag
import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.client.SignClient
import org.koin.core.KoinApplication

class WalletSampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        //TODO: register at https://walletconnect.com/register to get a project ID
        val serverUri = "wss://$WALLET_CONNECT_PROD_RELAY_URL?projectId=${BuildConfig.PROJECT_ID}"
        val relayClient = RelayClient(relayServerUrl = serverUri, connectionType = ConnectionType.AUTOMATIC, application = this)

        val initString = Sign.Params.Init(
            application = this,
            relay = relayClient,
            metadata = Sign.Model.AppMetaData(
                name = "Kotlin Wallet",
                description = "Wallet description",
                url = "example.wallet",
                icons = listOf("https://gblobscdn.gitbook.com/spaces%2F-LJJeCjcLrr53DcT1Ml7%2Favatar.png?alt=media"),
                redirect = "kotlin-wallet-wc:/request",
            ),
        )

        SignClient.initialize(initString) { error ->
            Log.e(tag(this), error.throwable.stackTraceToString())
        }
    }
}