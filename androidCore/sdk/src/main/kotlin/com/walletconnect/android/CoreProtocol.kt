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
import com.walletconnect.android.pairing.handler.PairingControllerInterface
import com.walletconnect.android.relay.ConnectionType
import com.walletconnect.android.relay.NetworkClientTimeout
import com.walletconnect.android.relay.RelayClient
import com.walletconnect.android.relay.RelayConnectionInterface
import com.walletconnect.android.sync.client.SyncClient
import com.walletconnect.android.sync.client.SyncInterface
import com.walletconnect.android.utils.plantTimber
import com.walletconnect.android.utils.projectId
import com.walletconnect.android.verify.VerifyClient
import com.walletconnect.android.verify.VerifyInterface
import org.koin.android.ext.koin.androidContext
import org.koin.core.KoinApplication
import org.koin.dsl.module


class CoreProtocol(private val koinApp: KoinApplication = wcKoinApp) : CoreInterface {
    override val Pairing: PairingInterface = PairingProtocol(koinApp)
    override val PairingController: PairingControllerInterface = PairingController(koinApp)
    override var Relay = RelayClient(koinApp)
    override val Echo: EchoInterface = EchoClient
    override val Verify: VerifyInterface = VerifyClient
    override val Sync: SyncInterface = SyncClient

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
                echoModule(),
                module { single { relay ?: Relay } },
                module { single { with(metaData) { AppMetaData(name, description, url, icons, Redirect(redirect)) } } },
                module { single { Echo } },
                coreJsonRpcModule(),
                corePairingModule(Pairing, PairingController),
                coreSyncModule(Sync),
                keyServerModule(keyServerUrl),
                explorerModule()
            )
        }

        if (relay == null) {
            Relay.initialize(relayServerUrl, connectionType, networkClientTimeout) { error -> onError(Core.Model.Error(error)) }
        }

        Verify.initialize(metaData.verifyUrl)
        Pairing.initialize()
        PairingController.initialize()
        Sync.initialize() { error -> onError(Core.Model.Error(error.throwable)) }
    }
}