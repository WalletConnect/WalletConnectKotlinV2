package com.walletconnect.notify.test.scenario

import com.walletconnect.android.Core
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.scope
import com.walletconnect.foundation.network.model.Relay
import com.walletconnect.notify.test.BuildConfig
import com.walletconnect.notify.test.utils.TestClient
import junit.framework.TestCase.fail
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import timber.log.Timber
import kotlin.time.Duration.Companion.seconds

// TODO: Replace testScope and runBlocking with kotlin.coroutines test dependency
//  Research why switching this to class made tests run 10x longer

class ClientInstrumentedActivityScenario : TestRule, ActivityScenario() {
    private val reconnectScope = CoroutineScope(Dispatchers.IO)
    private var primaryReconnectJob : Job? = null
    private var secondaryReconnectJob : Job? = null

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                beforeAll()
                base.evaluate()
                afterAll()
            }
        }
    }

    fun afterAll() {
        unregisterIdentities()
        scenario?.close()
        primaryReconnectJob?.cancel()
        secondaryReconnectJob?.cancel()
    }

    private fun unregisterIdentities() {
        Timber.d("afterAll unregister: start")
        runBlocking {
            TestClient.Primary.identitiesInteractor.unregisterIdentity(AccountId(TestClient.caip10account), "https://keys.walletconnect.com")
            TestClient.Secondary.identitiesInteractor.unregisterIdentity(AccountId(TestClient.caip10account), "https://keys.walletconnect.com")
        }
        Timber.d("afterAll unregister: finish")
    }

    private fun beforeAll() {
        runBlocking {
            initLogging()
            val isDappRelayReady = MutableStateFlow(false)
            val isWalletRelayReady = MutableStateFlow(false)

            val timeoutDuration = BuildConfig.TEST_TIMEOUT_SECONDS.seconds

            val dappRelayJob = TestClient.Secondary.Relay.eventsFlow.onEach { event ->
                when (event) {
                    is Relay.Model.Event.OnConnectionOpened<*> -> isDappRelayReady.compareAndSet(expect = false, update = true)
                    else -> {}
                }
            }.launchIn(scope)


            val walletRelayJob = TestClient.Primary.Relay.eventsFlow.onEach { event ->
                when (event) {
                    is Relay.Model.Event.OnConnectionOpened<*> -> isWalletRelayReady.compareAndSet(expect = false, update = true)
                    else -> {}
                }
            }.launchIn(scope)

            fun isEverythingReady() = isDappRelayReady.value && isWalletRelayReady.value && TestClient.Primary.isInitialized.value && TestClient.Secondary.isInitialized.value

            runCatching {
                withTimeout(timeoutDuration) {
                    while (!isEverythingReady()) {
                        delay(100)
                    }
                }
            }.fold(
                onSuccess = { Timber.d("Connection established and peers initialized with: ${TestClient.RELAY_URL}") },
                onFailure = { fail("Unable to establish connection OR initialize peers within $timeoutDuration") }
            )

            dappRelayJob.cancel()
            walletRelayJob.cancel()
            primaryReconnectJob = TestClient.Primary.Relay.isConnectionAvailable.onEach { isConnectionAvailable ->
                if (!isConnectionAvailable) {
                    reconnectScope.launch {
                        TestClient.Primary.Relay.connect { error: Core.Model.Error ->
                            Timber.e(error.throwable)
                        }
                    }
                }
            }.launchIn(scope)
            secondaryReconnectJob = TestClient.Secondary.Relay.isConnectionAvailable.onEach { isConnectionAvailable ->
                if (!isConnectionAvailable) {
                    reconnectScope.launch {
                        TestClient.Secondary.Relay.connect { error: Core.Model.Error ->
                            Timber.e(error.throwable)
                        }
                    }
                }
            }.launchIn(scope)
        }
    }
}