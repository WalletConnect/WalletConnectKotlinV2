package com.walletconnect.android.test.utils

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.walletconnect.android.BuildConfig
import com.walletconnect.android.Core
import com.walletconnect.android.CoreClient
import com.walletconnect.android.CoreProtocol
import com.walletconnect.android.di.overrideModule
import com.walletconnect.android.di.testJsonRpcModule
import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.di.AndroidCommonDITags
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.android.keyserver.domain.IdentitiesInteractor
import com.walletconnect.android.relay.ConnectionType
import com.walletconnect.android.relay.RelayClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.KoinApplication
import org.koin.core.qualifier.named
import timber.log.Timber

internal object TestClient {
    const val RELAY_URL = "wss://relay.walletconnect.com?projectId=${BuildConfig.PROJECT_ID}"
    private val app = ApplicationProvider.getApplicationContext<Application>()
    fun KoinApplication.Companion.createNewWCKoinApp(): KoinApplication = KoinApplication.init().apply { createEagerInstances() }

    object Primary {

        private val metadata = Core.Model.AppMetaData(
            name = "Kotlin E2E Primary",
            description = "Primary client for automation tests",
            url = "kotlin.e2e.primary",
            icons = listOf(),
            redirect = null
        )

        private var _isInitialized = MutableStateFlow(false)
        internal var isInitialized = _isInitialized.asStateFlow()

        private val coreProtocol = CoreClient.apply {
            Timber.d("Primary CP start: ")
            initialize(metadata, RELAY_URL, ConnectionType.MANUAL, app, onError = ::globalOnError)
            wcKoinApp.modules(testJsonRpcModule())
            Relay.connect(::globalOnError)
            _isInitialized.tryEmit(true)
            Timber.d("Primary CP finish: ")
        }


        internal val Relay get() = coreProtocol.Relay
        internal val jsonRpcInteractor: JsonRpcInteractorInterface by lazy { wcKoinApp.koin.get() }
        internal val keyManagementRepository: KeyManagementRepository by lazy { wcKoinApp.koin.get() }
        internal val identitiesInteractor: IdentitiesInteractor by lazy { wcKoinApp.koin.get() }
        internal val keyserverUrl: String by lazy { wcKoinApp.koin.get(named(AndroidCommonDITags.KEYSERVER_URL)) }
    }


    object Secondary {

        private val metadata = Core.Model.AppMetaData(
            name = "Kotlin E2E Secondary",
            description = "Secondary client for automation tests",
            url = "kotlin.e2e.secondary",
            icons = listOf(),
            redirect = null
        )

        private val secondaryKoinApp = KoinApplication.createNewWCKoinApp()

        private var _isInitialized = MutableStateFlow(false)
        internal var isInitialized = _isInitialized.asStateFlow()

        private val coreProtocol = CoreProtocol(secondaryKoinApp).apply {
            Timber.d("Secondary CP start: ")
            initialize(metadata, RELAY_URL, ConnectionType.MANUAL, app) { Timber.e(it.throwable) }

            // Override of previous Relay necessary for reinitialization of `eventsFlow`
            Relay = RelayClient(secondaryKoinApp)

            // Override of storage instances and depending objects
            secondaryKoinApp.modules(overrideModule(Relay, Pairing, PairingController, "test_secondary"), testJsonRpcModule())

            // Necessary reinit of Relay, Pairing and PairingController
            Relay.initialize(RELAY_URL, ConnectionType.MANUAL) { Timber.e(it) }
            Pairing.initialize()
            PairingController.initialize()

            Relay.connect(::globalOnError)
            _isInitialized.tryEmit(true)
            Timber.d("Secondary CP finish: ")
        }

        internal val Relay get() = coreProtocol.Relay
        internal val jsonRpcInteractor: JsonRpcInteractorInterface by lazy { secondaryKoinApp.koin.get() }
        internal val keyManagementRepository: KeyManagementRepository by lazy { secondaryKoinApp.koin.get() }

    }
}
