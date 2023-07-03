package com.walletconnect.android.test.activity

import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import com.walletconnect.android.BuildConfig
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.test.utils.TestClient
import com.walletconnect.foundation.network.model.Relay
import junit.framework.TestCase.assertTrue
import junit.framework.TestCase.fail
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.AfterClass
import org.junit.BeforeClass
import timber.log.Timber
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds

class WCInstrumentedActivityScenario {
    private var scenario: ActivityScenario<InstrumentedTestActivity>? = null
    private var scenarioLaunched: Boolean = false
    private val latch = CountDownLatch(1)
    private val testScope: CoroutineScope = CoroutineScope(Dispatchers.Default)

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

    init {
        initLogging()
        Timber.d("init")
    }

    @BeforeClass
    fun beforeAll() {
        runBlocking(testScope.coroutineContext) {
            Timber.d("beforeAll")
            val isDappRelayReady = MutableStateFlow(false)
            val isWalletRelayReady = MutableStateFlow(false)

            val timeoutDuration = BuildConfig.TEST_TIMEOUT_SECONDS.seconds

            val isEverythingReady: StateFlow<Boolean> = combine(isDappRelayReady, isWalletRelayReady, TestClient.Primary.isInitialized, TestClient.Secondary.isInitialized)
            { dappRelay, walletRelay, dappSign, walletSign -> (dappRelay && walletRelay && dappSign && walletSign) }.stateIn(scope, SharingStarted.Eagerly, false)

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

            runCatching {
                withTimeout(timeoutDuration) {
                    while (!(isDappRelayReady.value && isWalletRelayReady.value && TestClient.Primary.isInitialized.value && TestClient.Secondary.isInitialized.value)) {
                        delay(100)
                    }
                }
            }.fold(
                onSuccess = { Timber.d("Connection established with: ${TestClient.RELAY_URL}") },
                onFailure = { fail("Unable to establish connection within $timeoutDuration") }
            )

            dappRelayJob.cancel()
            walletRelayJob.cancel()
        }
    }

    @AfterClass
    fun afterAll() {
        Timber.d("afterAll")
        scenario?.close()
    }

    fun launch(timeoutSeconds: Long = 1, testCodeBlock: suspend (scope: CoroutineScope) -> Unit) {
        require(!scenarioLaunched) { "Scenario has already been launched!" }

        scenario = ActivityScenario.launch(InstrumentedTestActivity::class.java)
        scenarioLaunched = true

        scenario?.moveToState(Lifecycle.State.RESUMED)
        assert(scenario?.state?.isAtLeast(Lifecycle.State.RESUMED) == true)

        testScope.launch { testCodeBlock(testScope) }

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