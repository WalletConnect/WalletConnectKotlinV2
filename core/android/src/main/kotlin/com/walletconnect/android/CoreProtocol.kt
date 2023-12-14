package com.walletconnect.android

import android.app.Application
import com.walletconnect.android.di.coreStorageModule
import com.walletconnect.android.internal.common.di.coreCommonModule
import com.walletconnect.android.internal.common.di.coreCryptoModule
import com.walletconnect.android.internal.common.di.coreJsonRpcModule
import com.walletconnect.android.internal.common.di.corePairingModule
import com.walletconnect.android.internal.common.di.explorerModule
import com.walletconnect.android.internal.common.di.keyServerModule
import com.walletconnect.android.internal.common.di.pushModule
import com.walletconnect.android.internal.common.di.web3ModalModule
import com.walletconnect.android.internal.common.explorer.ExplorerInterface
import com.walletconnect.android.internal.common.explorer.ExplorerProtocol
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.ProjectId
import com.walletconnect.android.internal.common.model.Redirect
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.android.pairing.client.PairingInterface
import com.walletconnect.android.pairing.client.PairingProtocol
import com.walletconnect.android.pairing.handler.PairingController
import com.walletconnect.android.pairing.handler.PairingControllerInterface
import com.walletconnect.android.push.PushInterface
import com.walletconnect.android.push.client.PushClient
import com.walletconnect.android.relay.ConnectionType
import com.walletconnect.android.relay.NetworkClientTimeout
import com.walletconnect.android.relay.RelayClient
import com.walletconnect.android.relay.RelayConnectionInterface
import com.walletconnect.android.utils.plantTimber
import com.walletconnect.android.utils.projectId
import com.walletconnect.android.verify.client.VerifyClient
import com.walletconnect.android.verify.client.VerifyInterface
import org.koin.android.ext.koin.androidContext
import org.koin.core.KoinApplication
import org.koin.dsl.module

class CoreProtocol(private val koinApp: KoinApplication = wcKoinApp) : CoreInterface {
    override val Pairing: PairingInterface = PairingProtocol(koinApp)
    override val PairingController: PairingControllerInterface = PairingController(koinApp)
    override var Relay = RelayClient(koinApp)
    @Deprecated(message = "Replaced with Push")
    override val Echo: PushInterface = PushClient
    override val Push: PushInterface = PushClient
    override val Verify: VerifyInterface = VerifyClient(koinApp)
    override val Explorer: ExplorerInterface = ExplorerProtocol(koinApp)

    init {
        plantTimber()
    }

    override fun setDelegate(delegate: CoreInterface.Delegate) {
        Pairing.setDelegate(delegate)
    }

    companion object {
        val instance = CoreProtocol()
    }

    override fun initialize(
        metaData: Core.Model.AppMetaData,
        relayServerUrl: String,
        connectionType: ConnectionType,
        application: Application,
        relay: RelayConnectionInterface?,
        keyServerUrl: String?,
        networkClientTimeout: NetworkClientTimeout?,
        onError: (Core.Model.Error) -> Unit,
    ) {
        with(koinApp) {
            androidContext(application)
            modules(
                coreCommonModule(),
                coreCryptoModule(),
                module { single { ProjectId(relayServerUrl.projectId()) } },
                coreStorageModule(),
                pushModule(),
                module { single { relay ?: Relay } },
                module { single { with(metaData) { AppMetaData(name = name, description = description, url = url, icons = icons, redirect = Redirect(redirect)) } } },
                module { single { Echo } },
                module { single { Push } },
                module { single { Verify } },
                coreJsonRpcModule(),
                corePairingModule(Pairing, PairingController),
                keyServerModule(keyServerUrl),
                explorerModule(),
                web3ModalModule()
            )
        }

        if (relay == null) {
            Relay.initialize(relayServerUrl, connectionType, networkClientTimeout) { error -> onError(Core.Model.Error(error)) }
        }

        Verify.initialize()
        Pairing.initialize()
        PairingController.initialize()
    }
}