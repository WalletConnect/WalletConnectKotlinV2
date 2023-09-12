package com.walletconnect.android

import android.app.Application
import com.walletconnect.android.di.coreStorageModule
import com.walletconnect.android.echo.EchoClient
import com.walletconnect.android.echo.EchoInterface
import com.walletconnect.android.archive.ArchiveInterface
import com.walletconnect.android.archive.ArchiveProtocol
import com.walletconnect.android.internal.common.di.coreCommonModule
import com.walletconnect.android.internal.common.di.coreCryptoModule
import com.walletconnect.android.internal.common.di.coreJsonRpcModule
import com.walletconnect.android.internal.common.di.corePairingModule
import com.walletconnect.android.internal.common.di.coreSyncModule
import com.walletconnect.android.internal.common.di.echoModule
import com.walletconnect.android.internal.common.di.explorerModule
import com.walletconnect.android.internal.common.di.archiveModule
import com.walletconnect.android.internal.common.di.keyServerModule
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
import com.walletconnect.android.verify.client.VerifyClient
import com.walletconnect.android.verify.client.VerifyInterface
import org.koin.android.ext.koin.androidContext
import org.koin.core.KoinApplication
import org.koin.dsl.module

class CoreProtocol(private val koinApp: KoinApplication = wcKoinApp) : CoreInterface {
    override val Pairing: PairingInterface = PairingProtocol(koinApp)
    override val PairingController: PairingControllerInterface = PairingController(koinApp)
    override var Relay = RelayClient(koinApp)
    override val Echo: EchoInterface = EchoClient
    override val Verify: VerifyInterface = VerifyClient(koinApp)
    override val Sync: SyncInterface = SyncClient
    override val Archive: ArchiveInterface = ArchiveProtocol(koinApp)

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
                module { single { with(metaData) { AppMetaData(name = name, description = description, url = url, icons = icons, redirect = Redirect(redirect)) } } },
                module { single { Echo } },
                module { single { Verify } },
                coreJsonRpcModule(),
                corePairingModule(Pairing, PairingController),
                coreSyncModule(Sync),
                keyServerModule(keyServerUrl),
                explorerModule(),
                archiveModule(Archive, timeout = networkClientTimeout)
            )
        }

        if (relay == null) {
            Relay.initialize(relayServerUrl, networkClientTimeout, connectionType) { error -> onError(Core.Model.Error(error)) }
        }

        Verify.initialize(metaData.verifyUrl)
        Pairing.initialize()
        PairingController.initialize()
        Sync.initialize { error -> onError(Core.Model.Error(error.throwable)) }
    }

    override fun initialize(
        projectId: String,
        metaData: Core.Model.AppMetaData,
        application: Application,
        relayServerUrl: String?,
        networkClientTimeout: NetworkClientTimeout?,
        onError: (Core.Model.Error) -> Unit,
    ) {
        with(koinApp) {
            androidContext(application)
            modules(
                coreCommonModule(),
                coreCryptoModule(),
                module { single { ProjectId(projectId) } },
                coreStorageModule(),
                echoModule(),
                module { single<RelayConnectionInterface> { Relay } },
                coreJsonRpcModule(),
                corePairingModule(Pairing, PairingController),
                coreSyncModule(Sync),
                keyServerModule(),
                explorerModule(),
                archiveModule(Archive, timeout = networkClientTimeout),
                module {
                    single {
                        with(metaData) {
                            AppMetaData(name = name, description = description, url = url, icons = icons, redirect = Redirect(redirect))
                        }
                    }
                    single { Echo }
                    single { Verify }
                }
            )
        }

        Relay.initialize(relayServerUrl, networkClientTimeout) { error -> onError(Core.Model.Error(error)) }
        Verify.initialize(metaData.verifyUrl)
        Pairing.initialize()
        PairingController.initialize()
        Sync.initialize { error -> onError(Core.Model.Error(error.throwable)) }
    }
}