package com.walletconnect.android

import android.app.Application
import com.walletconnect.android.common.di.androidApiCryptoModule
import com.walletconnect.android.common.di.commonModule
import com.walletconnect.android.common.relay.RelayConnectionInterface
import com.walletconnect.android.common.wcKoinApp
import com.walletconnect.android.connection.ConnectionType
import com.walletconnect.android.pairing.PairingClient
import com.walletconnect.android.pairing.PairingInterface
import com.walletconnect.android.relay.RelayClient
import org.koin.android.ext.koin.androidContext

object CoreClient : CoreInterface, RelayConnectionInterface by RelayClient, PairingInterface by PairingClient {
    fun initialize(relayServerUrl: String, connectionType: ConnectionType, application: Application, appMetaData: Core.Model.AppMetaData) {

        wcKoinApp.run {
            androidContext(application)
            modules(commonModule(), androidApiCryptoModule())
        }

        RelayClient.initialize(relayServerUrl, connectionType, application)
        PairingClient.initialize(appMetaData)
    }
}