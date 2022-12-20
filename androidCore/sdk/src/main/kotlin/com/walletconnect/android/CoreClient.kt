package com.walletconnect.android

import android.app.Application
import com.walletconnect.android.di.coreStorageModule
import com.walletconnect.android.internal.common.di.androidApiCryptoModule
import com.walletconnect.android.internal.common.di.commonModule
import com.walletconnect.android.internal.common.di.jsonRpcModule
import com.walletconnect.android.internal.common.model.ProjectId
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.android.pairing.client.PairingInterface
import com.walletconnect.android.pairing.client.PairingProtocol
import com.walletconnect.android.pairing.engine.domain.PairingEngine
import com.walletconnect.android.pairing.handler.PairingController
import com.walletconnect.android.pairing.handler.PairingControllerInterface
import com.walletconnect.android.relay.ConnectionType
import com.walletconnect.android.relay.RelayClient
import com.walletconnect.android.relay.RelayConnectionInterface
import com.walletconnect.android.utils.projectId
import org.koin.android.ext.koin.androidContext
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

        println("kobe; core init")

        with(wcKoinApp) {
            androidContext(application)

            wcKoinApp.modules(
                coreStorageModule(),
                androidApiCryptoModule(),
                commonModule(),
                module { single { ProjectId(relayServerUrl.projectId()) } },
                jsonRpcModule(),
                pairingModule(),
                module { single { Relay } }
            )
        }


        Relay = if (relay != null) {
            relay
        } else {
            RelayClient.initialize(relayServerUrl, connectionType, application)
            RelayClient
        }

        PairingProtocol.initialize(metaData)
        PairingController.initialize()
    }

    fun setDelegate(delegate: CoreDelegate) {
        PairingProtocol.setDelegate(delegate)
    }
}

fun pairingModule() = module {
    single { PairingEngine() }
    single { CoreClient.Pairing }
    single<PairingControllerInterface> { PairingController }
}