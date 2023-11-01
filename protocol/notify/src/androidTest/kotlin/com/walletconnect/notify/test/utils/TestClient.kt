package com.walletconnect.notify.test.utils

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.walletconnect.android.Core
import com.walletconnect.android.CoreClient
import com.walletconnect.android.CoreProtocol
import com.walletconnect.android.cacao.signature.SignatureType
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.android.keyserver.domain.IdentitiesInteractor
import com.walletconnect.android.relay.ConnectionType
import com.walletconnect.android.relay.RelayClient
import com.walletconnect.android.utils.cacao.sign
import com.walletconnect.foundation.common.model.PrivateKey
import com.walletconnect.notify.BuildConfig
import com.walletconnect.notify.client.Notify
import com.walletconnect.notify.client.NotifyClient
import com.walletconnect.notify.client.NotifyProtocol
import com.walletconnect.notify.client.cacao.CacaoSigner
import com.walletconnect.notify.di.overrideModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.KoinApplication
import timber.log.Timber

internal object TestClient {
    const val RELAY_URL = "wss://relay.walletconnect.com?projectId=${BuildConfig.PROJECT_ID}"
    private val app = ApplicationProvider.getApplicationContext<Application>()
    fun KoinApplication.Companion.createNewWCKoinApp(): KoinApplication = init().apply { createEagerInstances() }

    internal const val account = "0x29163BFD38865971EfA5896e63bd6b7C8370E557"
    internal const val caip10account = "eip155:1:$account"
    internal const val publicKey = "5112cfe94b391100464b51af6f6714b4d7c21d25c92360b20b07278e722e033d426d26779f413330ceaf7cec2d5b4db91fe11bbb222955272f48478346549fe4"
    internal val privateKey = PrivateKey("7191ca005ee3696cf1f9d1f35d3baabaee55188db12fca85f662af7cc181f896")

    object Primary {

        private val metadata = Core.Model.AppMetaData(
            name = "Kotlin E2E Notify Primary",
            description = "Notify Primary Client for automation tests",
            url = "kotlin.e2e.notify.primary",
            icons = listOf(),
            redirect = null
        )

        private val coreProtocol = CoreClient.apply {
            Timber.d("Primary CP start: ")
            initialize(metadata, RELAY_URL, ConnectionType.MANUAL, app, onError = ::globalOnError)
            Relay.connect(::globalOnError)
        }

        private val initParams = Notify.Params.Init(coreProtocol)
        private var _isInitialized = MutableStateFlow(false)
        internal var isInitialized = _isInitialized.asStateFlow()
        internal val notifyClient = NotifyClient.apply {
            initialize(initParams, onError = { Timber.e(it.throwable) })
        }.also { notifyClient ->
            notifyClient.register(Notify.Params.Registration(caip10account, metadata.url, { message -> CacaoSigner.sign(message, privateKey.keyAsBytes, SignatureType.EIP191) }), { identityKey ->
                Timber.d("Primary CP finish: $identityKey")
                _isInitialized.tryEmit(true)
            }, { error ->
                Timber.e(error.throwable)
                throw error.throwable
            })
        }

        internal val Relay get() = coreProtocol.Relay
        internal val Pairing = coreProtocol.Pairing
        internal val identitiesInteractor: IdentitiesInteractor by lazy { wcKoinApp.koin.get() }
    }

    object Secondary {

        private val metadata = Core.Model.AppMetaData(
            name = "Kotlin E2E Notify Secondary",
            description = "Notify Secondary Client for automation tests",
            url = "kotlin.e2e.notify.secondary",
            icons = listOf(),
            redirect = null
        )

        private val secondaryKoinApp = KoinApplication.createNewWCKoinApp()

        private val coreProtocol = CoreProtocol(secondaryKoinApp).apply {
            Timber.d("Secondary CP start: ")
            initialize(metadata, RELAY_URL, ConnectionType.MANUAL, app) { Timber.e(it.throwable) }

            // Override of previous Relay necessary for reinitialization of `eventsFlow`
            Relay = RelayClient(secondaryKoinApp)

            // Override of storage instances and depending objects
            secondaryKoinApp.modules(overrideModule(Relay, Pairing, PairingController, "test_secondary"))

            // Necessary reinit of Relay, Pairing and PairingController
            Relay.initialize(RELAY_URL, ConnectionType.MANUAL) { Timber.e(it) }
            Pairing.initialize()
            PairingController.initialize()

            Relay.connect(::globalOnError)
        }

        private val initParams = Notify.Params.Init(coreProtocol)
        private var _isInitialized = MutableStateFlow(false)
        internal var isInitialized = _isInitialized.asStateFlow()

        internal val notifyClient = NotifyProtocol(secondaryKoinApp).apply {
            initialize(initParams, onError = { Timber.e(it.throwable) })
        }.also { notifyClient ->
            notifyClient.register(Notify.Params.Registration(caip10account, metadata.url, { message -> CacaoSigner.sign(message, privateKey.keyAsBytes, SignatureType.EIP191) }), { identityKey ->
                Timber.d("Secondary CP finish: $identityKey")
                _isInitialized.tryEmit(true)
            }, { error ->
                Timber.e(error.throwable)
                throw error.throwable
            })
        }

        internal val Relay get() = coreProtocol.Relay
        internal val Pairing = coreProtocol.Pairing
        internal val identitiesInteractor: IdentitiesInteractor by lazy { secondaryKoinApp.koin.get() }
    }
}