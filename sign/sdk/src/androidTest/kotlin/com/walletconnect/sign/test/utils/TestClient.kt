package com.walletconnect.sign.test.utils

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.walletconnect.android.Core
import com.walletconnect.android.CoreClient
import com.walletconnect.android.CoreProtocol
import com.walletconnect.android.relay.ConnectionType
import com.walletconnect.android.relay.RelayClient
import com.walletconnect.sign.BuildConfig
import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.client.SignClient
import com.walletconnect.sign.client.SignProtocol
import com.walletconnect.sign.di.overrideModule
import junit.framework.TestCase.fail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.KoinApplication
import timber.log.Timber

internal object TestClient {
    const val RELAY_URL = "wss://relay.walletconnect.com?projectId=${BuildConfig.PROJECT_ID}"
    private val app = ApplicationProvider.getApplicationContext<Application>()
    fun KoinApplication.Companion.createNewWCKoinApp(): KoinApplication = KoinApplication.init().apply { createEagerInstances() }

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

        private val dappKoinApp = KoinApplication.createNewWCKoinApp()

        private val coreProtocol = CoreProtocol(dappKoinApp).apply {
            Timber.d("Dapp CP start: ")
            initialize(metadata, RELAY_URL, ConnectionType.MANUAL, app) { Timber.e(it.throwable) }

            // Override of previous Relay necessary for reinitialization of `eventsFlow`
            Relay = RelayClient(dappKoinApp)

//            // Override of previous Verify
//            Verify = VerifyClient(dappKoinApp)

            // Override of storage instances and depending objects
            dappKoinApp.modules(overrideModule(Relay, Pairing, PairingController))

            // Necessary reinit of Relay, Pairing and PairingController
            Relay.initialize(RELAY_URL, ConnectionType.MANUAL) { Timber.e(it) }
            Pairing.initialize()
            PairingController.initialize()

            Relay.connect(::globalOnError)
        }

        private val initParams = Sign.Params.Init(coreProtocol)
        private var _isInitialized = MutableStateFlow(false)
        internal var isInitialized = _isInitialized.asStateFlow()
        internal val signClient = SignProtocol(dappKoinApp).apply {
            initialize(initParams, onSuccess = { _isInitialized.tryEmit(true) }, onError = { Timber.e(it.throwable) })
            Timber.d("Dapp CP finish: ")
        }

        internal val Relay get() = coreProtocol.Relay
        internal val Pairing = coreProtocol.Pairing
    }
}

fun pair(onPairSuccess: (pairing: Core.Model.Pairing) -> Unit) {
    TestClient.Dapp.Pairing.getPairings().let { pairings ->
        if (pairings.isEmpty()) {
            Timber.d("pairings.isEmpty() == true")

            val pairing: Core.Model.Pairing = (TestClient.Dapp.Pairing.create(onError = ::globalOnError) ?: fail("Unable to create a Pairing")) as Core.Model.Pairing
            Timber.d("DappClient.pairing.create: $pairing")

            TestClient.Wallet.Pairing.pair(Core.Params.Pair(pairing.uri), onError = ::globalOnError, onSuccess = {
                Timber.d("WalletClient.pairing.pair: $pairing")
                onPairSuccess(pairing)
            })
        } else {
            Timber.d("pairings.isEmpty() == false")
            fail("Pairing already exists. Storage must be cleared in between runs")
        }
    }
}

fun pairAndConnect() {
    pair { pairing -> dappClientConnect(pairing) }
}