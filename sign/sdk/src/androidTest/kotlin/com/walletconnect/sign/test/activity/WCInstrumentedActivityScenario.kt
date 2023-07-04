package com.walletconnect.sign.test.activity

import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import com.walletconnect.android.internal.common.scope
import com.walletconnect.foundation.network.model.Relay
import com.walletconnect.sign.test.BuildConfig
import com.walletconnect.sign.test.utils.TestClient
import junit.framework.TestCase.assertTrue
import junit.framework.TestCase.fail
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds

// TODO: Replace testScope and runBlocking with kotlin.coroutines test dependency
//  Research why switching this to class made tests run 10x longer

class WCInstrumentedActivityScenario : TestRule {
    private var scenario: ActivityScenario<InstrumentedTestActivity>? = null
    private var scenarioLaunched: Boolean = false
    private val latch = CountDownLatch(1)
    private val testScope: CoroutineScope = CoroutineScope(Dispatchers.Default)

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                beforeAll()
                base.evaluate()
                afterAll()
            }
        }
    }

    private fun initLogging() {
        if (Timber.treeCount == 0) {
            Timber.plant(
                object : Timber.DebugTree() {
                    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                        super.log(priority, "WalletConnectV2", message, t)
                    }
                }
            )
        }
    }

    private fun beforeAll() {
        runBlocking {
            initLogging()
            Timber.d("kobe; beforeAll")
            val isDappRelayReady = MutableStateFlow(false)
            val isWalletRelayReady = MutableStateFlow(false)

            val timeoutDuration = BuildConfig.TEST_TIMEOUT_SECONDS.seconds

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
                onSuccess = { Timber.d("Connection established with: ${TestClient.RELAY_URL}") },
                onFailure = {
                    //todo change message
                    fail("Unable to establish connection within $timeoutDuration")
                }
            )

            dappRelayJob.cancel()
            walletRelayJob.cancel()
        }
    }

    private fun afterAll() {
        Timber.d("afterAll")
        scenario?.close()
    }

    fun launch(timeoutSeconds: Long = 1, testCodeBlock: suspend () -> Unit) {

        println("kobe; Launch")

        require(!scenarioLaunched) { "Scenario has already been launched!" }

        scenario = ActivityScenario.launch(InstrumentedTestActivity::class.java)
        scenarioLaunched = true

        scenario?.moveToState(Lifecycle.State.RESUMED)
        assert(scenario?.state?.isAtLeast(Lifecycle.State.RESUMED) == true)

        testScope.launch { testCodeBlock() }

        try {
            assertTrue(latch.await(timeoutSeconds, TimeUnit.SECONDS))
        } catch (exception: Exception) {
            fail(exception.message)
        }
    }

    fun closeAsSuccess() {
        latch.countDown()
    }
}