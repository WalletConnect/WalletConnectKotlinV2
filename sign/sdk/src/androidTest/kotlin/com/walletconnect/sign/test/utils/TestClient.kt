package com.walletconnect.sign.test.utils

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.walletconnect.android.Core
import com.walletconnect.android.CoreClient
import com.walletconnect.android.CoreProtocol
import com.walletconnect.android.internal.common.createNewWCKoinApp
import com.walletconnect.android.relay.ConnectionType
import com.walletconnect.android.relay.RelayClient
import com.walletconnect.sign.BuildConfig
import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.client.SignClient
import com.walletconnect.sign.client.SignProtocol
import com.walletconnect.sign.di.overrideModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.KoinApplication
import timber.log.Timber


internal object TestClient {
    const val RELAY_URL = "wss://relay.walletconnect.com?projectId=${BuildConfig.PROJECT_ID}"
    private val app = ApplicationProvider.getApplicationContext<Application>()

    object Wallet {

        private val metadata = Core.Model.AppMetaData(
            name = "Kotlin E2E Wallet",
            description = "Wallet for automation tests",
            url = "kotlin.e2e.wallet",
            icons = listOf(),
            redirect = null
        )

        private val coreProtocol = CoreClient.apply {
            Timber.d("Wallet CP start: ")
            initialize(metadata, RELAY_URL, ConnectionType.MANUAL, app, onError = ::globalOnError)
            Relay.connect(::globalOnError)
        }

        private val initParams = Sign.Params.Init(coreProtocol)
        private var _isInitialized = MutableStateFlow(false)
        internal var isInitialized = _isInitialized.asStateFlow()
        internal val signClient = SignClient.apply {
            initialize(initParams, onSuccess = { _isInitialized.tryEmit(true) }, onError = { Timber.e(it.throwable) })
            Timber.d("Wallet CP finish: ")
        }

        internal val Relay get() = coreProtocol.Relay
        internal val Pairing = coreProtocol.Pairing
    }


    object Dapp {

        private val metadata = Core.Model.AppMetaData(
            name = "Kotlin E2E Dapp",
            description = "Dapp for automation tests",
            url = "kotlin.e2e.dapp",
            icons = listOf(),
            redirect = null
        )

        private val coreProtocol = CoreProtocol().apply {
            Timber.d("Dapp CP start: ")
            koinApp = KoinApplication.createNewWCKoinApp()

            initialize(metadata, RELAY_URL, ConnectionType.MANUAL, app) { Timber.e(it.throwable) }

            // Override of previous Relay necessary for reinitialization of `eventsFlow`
            Relay = RelayClient()

            // Override of storage instances and depending objects
            koinApp.modules(overrideModule(Relay, Pairing, PairingController))

            // Necessary reinit of Relay, Pairing and PairingController
            Relay.initialize(koinApp, RELAY_URL, ConnectionType.MANUAL) { Timber.e(it) }
            Pairing.initialize(koinApp)
            PairingController.initialize(koinApp)

            Relay.connect(::globalOnError)
        }

        private val initParams = Sign.Params.Init(coreProtocol)
        private var _isInitialized = MutableStateFlow(false)
        internal var isInitialized = _isInitialized.asStateFlow()
        internal val signClient = SignProtocol().apply {
            initialize(initParams, onSuccess = { _isInitialized.tryEmit(true) }, onError = { Timber.e(it.throwable) })
            Timber.d("Dapp CP finish: ")
        }

        internal val Relay get() = coreProtocol.Relay
        internal val Pairing = coreProtocol.Pairing
    }
}

val WalletSignClient = TestClient.Wallet.signClient
val DappSignClient = TestClient.Dapp.signClient