package com.walletconnect.android

import android.app.Application
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.android.pairing.client.PairingInterface
import com.walletconnect.android.pairing.client.PairingProtocol
import com.walletconnect.android.pairing.engine.domain.PairingEngine
import com.walletconnect.android.pairing.handler.PairingHandler
import com.walletconnect.android.pairing.handler.PairingHandlerInterface
import com.walletconnect.android.relay.ConnectionType
import com.walletconnect.android.relay.RelayClient
import com.walletconnect.android.relay.RelayConnectionInterface
import org.koin.dsl.module

object CoreClient {
    val Pairing: PairingInterface = PairingProtocol
    var Relay: RelayConnectionInterface = RelayClient

    interface CoreDelegate : PairingInterface.Delegate

    fun initialize(
        metaData: Core.Model.AppMetaData,
        relayServerUrl: String,
        connectionType: ConnectionType,
        application: Application,
        relay: RelayConnectionInterface? = null
    ) {
        if (relay != null) {
            Relay = relay
        } else {
            RelayClient.initialize(relayServerUrl, connectionType, application)
        }
        wcKoinApp.modules(
            module {
                single { PairingEngine() }
                single { Pairing }
                single<PairingHandlerInterface> { PairingHandler }
                single { Relay }
            }
        )
        PairingProtocol.initialize(metaData)
        PairingHandler.initialize()
    }

    fun setDelegate(delegate: CoreDelegate) {
        PairingProtocol.setDelegate(delegate)
    }
}