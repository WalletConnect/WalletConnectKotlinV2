package com.walletconnect.android

import android.app.Application
import android.content.SharedPreferences
import com.walletconnect.android.di.coreStorageModule
import com.walletconnect.android.internal.common.di.AndroidCommonDITags
import com.walletconnect.android.internal.common.di.KEY_CLIENT_ID
import com.walletconnect.android.internal.common.di.coreAndroidNetworkModule
import com.walletconnect.android.internal.common.di.coreCommonModule
import com.walletconnect.android.internal.common.di.coreCryptoModule
import com.walletconnect.android.internal.common.di.coreJsonRpcModule
import com.walletconnect.android.internal.common.di.corePairingModule
import com.walletconnect.android.internal.common.di.explorerModule
import com.walletconnect.android.internal.common.di.keyServerModule
import com.walletconnect.android.internal.common.di.pulseModule
import com.walletconnect.android.internal.common.di.pushModule
import com.walletconnect.android.internal.common.di.web3ModalModule
import com.walletconnect.android.internal.common.explorer.ExplorerInterface
import com.walletconnect.android.internal.common.explorer.ExplorerProtocol
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.ProjectId
import com.walletconnect.android.internal.common.model.Redirect
import com.walletconnect.android.internal.common.model.TelemetryEnabled
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
import com.walletconnect.android.utils.isValidRelayServerUrl
import com.walletconnect.android.utils.plantTimber
import com.walletconnect.android.utils.projectId
import com.walletconnect.android.verify.client.VerifyClient
import com.walletconnect.android.verify.client.VerifyInterface
import org.koin.android.ext.koin.androidContext
import org.koin.core.KoinApplication
import org.koin.core.qualifier.named
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
        telemetryEnabled: Boolean,
        onError: (Core.Model.Error) -> Unit
    ) {
        try {
            require(relayServerUrl.isValidRelayServerUrl()) { "Check the schema and projectId parameter of the Server Url" }

            setup(
                application = application,
                serverUrl = relayServerUrl,
                projectId = relayServerUrl.projectId(),
                telemetryEnabled = telemetryEnabled,
                connectionType = connectionType,
                networkClientTimeout = networkClientTimeout,
                relay = relay,
                onError = onError,
                metaData = metaData,
                keyServerUrl = keyServerUrl
            )
        } catch (e: Exception) {
            onError(Core.Model.Error(e))
        }
    }

    override fun initialize(
        application: Application,
        projectId: String,
        metaData: Core.Model.AppMetaData,
        connectionType: ConnectionType,
        relay: RelayConnectionInterface?,
        keyServerUrl: String?,
        networkClientTimeout: NetworkClientTimeout?,
        telemetryEnabled: Boolean,
        onError: (Core.Model.Error) -> Unit
    ) {
        try {
            require(projectId.isNotEmpty()) { "Project Id cannot be empty" }

            setup(
                application = application,
                projectId = projectId,
                telemetryEnabled = telemetryEnabled,
                connectionType = connectionType,
                networkClientTimeout = networkClientTimeout,
                relay = relay,
                onError = onError,
                metaData = metaData,
                keyServerUrl = keyServerUrl
            )
        } catch (e: Exception) {
            onError(Core.Model.Error(e))
        }
    }

    private fun CoreProtocol.setup(
        application: Application,
        serverUrl: String? = null,
        projectId: String,
        telemetryEnabled: Boolean,
        connectionType: ConnectionType,
        networkClientTimeout: NetworkClientTimeout?,
        relay: RelayConnectionInterface?,
        onError: (Core.Model.Error) -> Unit,
        metaData: Core.Model.AppMetaData,
        keyServerUrl: String?
    ) {
        val bundleId: String = application.packageName
        val relayServerUrl = if (serverUrl.isNullOrEmpty()) "wss://relay.walletconnect.org?projectId=$projectId" else serverUrl

        with(koinApp) {
            androidContext(application)
            modules(
                module { single { ProjectId(projectId) } },
                module { single(named(AndroidCommonDITags.TELEMETRY_ENABLED)) { TelemetryEnabled(telemetryEnabled) } },
                coreAndroidNetworkModule(relayServerUrl, connectionType, BuildConfig.SDK_VERSION, networkClientTimeout, bundleId),
                coreCommonModule(),
                coreCryptoModule(),
            )

            if (relay == null) {
                Relay.initialize(connectionType) { error -> onError(Core.Model.Error(error)) }
            }

            modules(
                coreStorageModule(bundleId = bundleId),
                module { single(named(AndroidCommonDITags.CLIENT_ID)) { requireNotNull(get<SharedPreferences>().getString(KEY_CLIENT_ID, null)) } },
                pushModule(),
                module { single { relay ?: Relay } },
                module {
                    single {
                        with(metaData) {
                            AppMetaData(
                                name = name,
                                description = description,
                                url = url,
                                icons = icons,
                                redirect = Redirect(native = redirect, universal = appLink, linkMode = linkMode)
                            )
                        }
                    }
                },
                module { single { Echo } },
                module { single { Push } },
                module { single { Verify } },
                coreJsonRpcModule(),
                corePairingModule(Pairing, PairingController),
                keyServerModule(keyServerUrl),
                explorerModule(),
                web3ModalModule(),
                pulseModule(bundleId)
            )
        }

        Pairing.initialize()
        PairingController.initialize()
        Verify.initialize()
    }
}