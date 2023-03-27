package com.walletconnect.android

import android.app.Application
import com.walletconnect.android.di.coreStorageModule
import com.walletconnect.android.echo.EchoClient
import com.walletconnect.android.echo.EchoInterface
import com.walletconnect.android.internal.common.di.*
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.ProjectId
import com.walletconnect.android.internal.common.model.Redirect
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.android.pairing.client.PairingInterface
import com.walletconnect.android.pairing.client.PairingProtocol
import com.walletconnect.android.pairing.handler.PairingController
import com.walletconnect.android.relay.ConnectionType
import com.walletconnect.android.relay.RelayClient
import com.walletconnect.android.relay.RelayConnectionInterface
import com.walletconnect.android.utils.plantTimber
import com.walletconnect.android.utils.projectId
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

object CoreClient {
    val Pairing: PairingInterface = PairingProtocol
    var Relay: RelayConnectionInterface = RelayClient
    val Echo: EchoInterface = EchoClient

    interface CoreDelegate : PairingInterface.Delegate

    fun initialize(
        metaData: Core.Model.AppMetaData,
        relayServerUrl: String,
        connectionType: ConnectionType,
        application: Application,
        relay: RelayConnectionInterface? = null,
        keyServerUrl: String? = null,
        onError: (Core.Model.Error) -> Unit,
    ) {
        plantTimber()
        with(wcKoinApp) {
            androidContext(application)
            modules(
                coreCommonModule(),
                coreCryptoModule(),
                module { single { ProjectId(relayServerUrl.projectId()) } },
                coreStorageModule(),
                echoModule(),
                module { single { relay ?: RelayClient } },
                module { single { with(metaData) { AppMetaData(name, description, url, icons, Redirect(redirect)) } } },
                module { single { Echo } },
                coreJsonRpcModule(),
                corePairingModule(Pairing),
                keyServerModule(keyServerUrl),
            )
        }

        if (relay == null) {
            RelayClient.initialize(relayServerUrl, connectionType) { error -> onError(Core.Model.Error(error)) }
        }

        PairingProtocol.initialize()
        PairingController.initialize()
    }

    fun setDelegate(delegate: CoreDelegate) {
        PairingProtocol.setDelegate(delegate)
    }
}