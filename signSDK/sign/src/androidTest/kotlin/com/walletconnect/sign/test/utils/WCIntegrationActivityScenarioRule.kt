package com.walletconnect.sign.test.utils

import android.net.Uri
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.walletconnect.sign.BuildConfig
import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.client.SignProtocol
import com.walletconnect.sign.core.model.client.Relay
import com.walletconnect.sign.core.scope.scope
import com.walletconnect.sign.util.Logger
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.junit.Assert
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.rules.ExternalResource
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

// Source: https://gist.github.com/zawadz88/f057c70d3061454207ccd56e0add81c6#file-lazyactivityscenariorule-kt
class WCIntegrationActivityScenarioRule : ExternalResource() {
    private var scenario: ActivityScenario<IntegrationTestActivity>? = null
    private var scenarioLaunched: Boolean = false
    private val latch = CountDownLatch(1)
    private val testScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
    private val app = ApplicationProvider.getApplicationContext<IntegrationTestApplication>()

    private val relayUrl = BuildConfig.WC_RELAY_URL.split(URL_DELIMETER).let { (scheme, hostName) ->
        Uri.Builder()
            .scheme(scheme)
            .authority(hostName)
            .appendQueryParameter(PROJECT_ID_KEY, BuildConfig.PROJECT_ID)
            .build()
            .toString()
    }

    private val walletMetadata = Sign.Model.AppMetaData(
        name = "Kotlin Wallet",
        description = "Wallet description",
        url = "example.wallet",
        icons = listOf("https://gblobscdn.gitbook.com/spaces%2F-LJJeCjcLrr53DcT1Ml7%2Favatar.png?alt=media")
    )

    private val walletParams = Sign.Params.Init(
        application = app,
        relayServerUrl = relayUrl,
        metadata = walletMetadata,
        connectionType = Sign.ConnectionType.MANUAL
    )


    internal val walletClient = SignProtocol().apply {
        initialize(walletParams) {}
        blockUntilInitialized()
        relay.connect { }
    }


    private val dappMetadata = Sign.Model.AppMetaData(
        name = "Kotlin Dapp",
        description = "Dapp description",
        url = "example.dapp",
        icons = listOf("https://gblobscdn.gitbook.com/spaces%2F-LJJeCjcLrr53DcT1Ml7%2Favatar.png?alt=media")
    )

    private val dappParams = Sign.Params.Init(
        application = app,
        relayServerUrl = relayUrl,
        metadata = dappMetadata,
        connectionType = Sign.ConnectionType.MANUAL
    )

    internal val dappClient = SignProtocol().apply {
        initialize(dappParams) {}
        blockUntilInitialized()
        relay.connect { }
    }

    override fun before() {
        runBlocking {
            val isDappReady = MutableStateFlow(false)
            val isWalletReady = MutableStateFlow(false)
            val areBothReady: StateFlow<Boolean> =
                combine(isDappReady, isWalletReady) { dapp, wallet -> dapp && wallet }.stateIn(scope, SharingStarted.Eagerly, false)

            val dappJob = dappClient.relay.eventsFlow.onEach { event ->
                when (event) {
                    is Relay.Model.Event.OnConnectionOpened<*> -> isDappReady.compareAndSet(expect = false, update = true)
                    else -> {}
                }
            }.launchIn(scope)

            val walletJob = walletClient.relay.eventsFlow.onEach { event ->
                when (event) {
                    is Relay.Model.Event.OnConnectionOpened<*> -> isWalletReady.compareAndSet(expect = false, update = true)
                    else -> {}
                }
            }.launchIn(scope)

            while (!areBothReady.value) {
                delay(100)
            }

            dappJob.cancel()
            walletJob.cancel()
            Logger.log("Connection established with: $relayUrl")
        }
    }

    override fun after() {
        scenario?.close()
    }

    fun launch(timeoutSeconds: Long = 1, testCodeBlock: () -> Unit) {
        require(!scenarioLaunched) { "Scenario has already been launched!" }

        scenario = ActivityScenario.launch(IntegrationTestActivity::class.java)
        scenarioLaunched = true

        scenario?.moveToState(Lifecycle.State.RESUMED)
        assert(scenario?.state?.isAtLeast(Lifecycle.State.RESUMED) == true)

        testScope.launch { testCodeBlock() }

        try {
            assertTrue(latch.await(timeoutSeconds, TimeUnit.SECONDS))
        } catch (exception: InterruptedException) {
            Assert.fail(exception.stackTraceToString())
            testScope.cancel()
        } catch (exception: IllegalArgumentException) {
            Assert.fail(exception.stackTraceToString())
            testScope.cancel()
        }
    }

    fun close() {
        latch.countDown()
    }

    companion object {
        const val URL_DELIMETER = "://"
        const val PROJECT_ID_KEY = "projectId"
    }
}