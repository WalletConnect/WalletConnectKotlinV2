package com.walletconnect.sign.test.scenario

import com.walletconnect.android.internal.common.scope
import com.walletconnect.foundation.network.model.Relay
import com.walletconnect.sign.test.BuildConfig
import com.walletconnect.sign.test.utils.TestClient
import junit.framework.TestCase.fail
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import timber.log.Timber
import kotlin.time.Duration.Companion.seconds

// TODO: Replace testScope and runBlocking with kotlin.coroutines test dependency
//  Research why switching this to class made tests run 10x longer

class SignClientInstrumentedActivityScenario : TestRule, SignActivityScenario() {
    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                beforeAll()
                base.evaluate()
                afterAll()
            }
        }
    }

    private fun beforeAll() {
        runBlocking {
            initLogging()
            val isDappRelayReady = MutableStateFlow(false)
            val isWalletRelayReady = MutableStateFlow(false)

            val timeoutDuration = BuildConfig.TEST_TIMEOUT_SECONDS.seconds
            TestClient.Wallet.Relay.isLoggingEnabled = true
            TestClient.Dapp.Relay.isLoggingEnabled = true

            val dappRelayJob = TestClient.Dapp.Relay.eventsFlow.onEach { event ->
                when (event) {
                    is Relay.Model.Event.OnConnectionOpened<*> -> isDappRelayReady.compareAndSet(expect = false, update = true)
                    else -> {}
                }
            }.launchIn(scope)


            val walletRelayJob = TestClient.Wallet.Relay.eventsFlow.onEach { event ->
                when (event) {
                    is Relay.Model.Event.OnConnectionOpened<*> -> isWalletRelayReady.compareAndSet(expect = false, update = true)
                    else -> {}
                }
            }.launchIn(scope)

            fun isEverythingReady() = isDappRelayReady.value && isWalletRelayReady.value && TestClient.Wallet.isInitialized.value && TestClient.Dapp.isInitialized.value

            runCatching {
                withTimeout(timeoutDuration) {
                    while (!isEverythingReady()) {
                        delay(100)
                    }
                }
            }.fold(
                onSuccess = { Timber.d("Connection established and peers initialized successfully") },
                onFailure = { fail("Unable to establish connection OR initialize peers within $timeoutDuration") }
            )

            dappRelayJob.cancel()
            walletRelayJob.cancel()
        }
    }
}