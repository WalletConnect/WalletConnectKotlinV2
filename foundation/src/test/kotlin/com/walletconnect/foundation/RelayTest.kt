package com.walletconnect.foundation

import com.walletconnect.foundation.crypto.data.repository.JwtRepository
import com.walletconnect.foundation.di.FoundationDITags
import com.walletconnect.foundation.di.commonModule
import com.walletconnect.foundation.di.cryptoModule
import com.walletconnect.foundation.di.networkModule
import com.walletconnect.foundation.network.BaseRelayClient
import com.walletconnect.foundation.network.RelayInterface
import com.walletconnect.foundation.network.data.service.RelayService
import com.walletconnect.foundation.network.model.Relay
import com.walletconnect.util.addUserAgent
import com.walletconnect.util.bytesToHex
import io.mockk.every
import io.mockk.spyk
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.junit.jupiter.api.Test
import org.koin.core.KoinApplication
import org.koin.core.qualifier.named
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.fail
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime


@ExperimentalCoroutinesApi
class RelayTest {
    private val testProjectId: String = System.getProperty("TEST_PROJECT_ID")
    private val testRelayUrl: String = System.getProperty("TEST_RELAY_URL")
    private val serverUrl = "$testRelayUrl?projectId=$testProjectId"
    private val sdkVersion: String = System.getProperty("SDK_VERSION")
    private val testJob: CompletableJob = SupervisorJob()
    private val testScope: CoroutineScope = CoroutineScope(testJob + Dispatchers.IO)

    @ExperimentalTime
    @Test
    fun `One client sends unencrypted message, second one receives it`() {
        val isTestSuccessful = MutableStateFlow(false)
        val testTopic = Random.nextBytes(32).bytesToHex()
        val testMessage = "testMessage"

        val (clientA: RelayInterface, clientB: RelayInterface) = initTwoClients()

        // Listen to incoming messages/requests
        clientB.subscriptionRequest.onEach {
            println("ClientB subscriptionRequest: $it")
            assertEquals(testMessage, it.params.subscriptionData.message)
            isTestSuccessful.compareAndSet(expect = false, update = true)
        }.launchIn(testScope)

        //Await connection
        measureAwaitingForConnection(clientA, clientB)

        //Subscribe to topic
        clientB.subscribe(testTopic) { println("ClientB subscribe: $it") }

        //Publish message
        clientA.publish(testTopic, testMessage, Relay.Model.IrnParams(0, 20000L)) { println("ClientA topic: $testTopic, publish: $it") }

        //Lock until is finished or timed out
        runBlocking {
            val start = System.currentTimeMillis()
            // Await test finish or check if timeout occurred
            while (!isTestSuccessful.value && !didTimeout(start, 5000L)) {
                delay(10)
            }
            // Success or fail
            if (isTestSuccessful.value) return@runBlocking else fail("Test timeout")
        }
    }

    @ExperimentalTime
    private fun measureAwaitingForConnection(clientA: RelayInterface, clientB: RelayInterface) {
        println("Connection established after ${measureTime { awaitConnection(clientA, clientB) }.inWholeMilliseconds} ms with: $testRelayUrl")
    }

    private fun startLoggingClientEventsFlow(client: RelayInterface, tag: String) =
        client.eventsFlow.onEach { println("$tag eventsFlow: $it") }.launchIn(testScope)

    private fun initTwoClients(): Pair<RelayInterface, RelayInterface> {

        val koinAppA: KoinApplication = KoinApplication.init()
            .apply { modules(commonModule(), cryptoModule()) }.also { koinApp ->
                val jwt = koinApp.koin.get<JwtRepository>().generateJWT(serverUrl)
                koinApp.modules(networkModule(serverUrl.addUserAgent(), sdkVersion, jwt))
            }

        val koinAppB: KoinApplication = KoinApplication.init()
            .apply { modules(commonModule(), cryptoModule()) }.also { koinApp ->
                val jwt = koinApp.koin.get<JwtRepository>().generateJWT(serverUrl)
                koinApp.modules(networkModule(serverUrl.addUserAgent(), sdkVersion, jwt))
            }

        val clientA: BaseRelayClient = spyk<BaseRelayClient>(koinAppA.koin.get<BaseRelayClient>())
        val clientB: BaseRelayClient = koinAppB.koin.get<BaseRelayClient>()

        clientA.relayService = koinAppA.koin.get<RelayService>(named(FoundationDITags.RELAY_SERVICE))
        clientB.relayService = koinAppA.koin.get<RelayService>(named(FoundationDITags.RELAY_SERVICE))

        startLoggingClientEventsFlow(clientA, "ClientA")
        startLoggingClientEventsFlow(clientB, "ClientB")

        return (clientA to clientB)
    }

    private fun didTimeout(start: Long, timeout: Long): Boolean = System.currentTimeMillis() - start > timeout

    private fun awaitConnection(clientA: RelayInterface, clientB: RelayInterface) = runBlocking {
        val isClientAReady = MutableStateFlow(false)
        val isClientBReady = MutableStateFlow(false)
        val areBothReady: StateFlow<Boolean> =
            combine(isClientAReady, isClientBReady) { clientA: Boolean, clientB: Boolean -> clientA && clientB }.stateIn(
                testScope,
                SharingStarted.Eagerly,
                false
            )

        val clientAJob = clientA.eventsFlow.onEach { event ->
            when (event) {
                is Relay.Model.Event.OnConnectionOpened<*> -> isClientAReady.compareAndSet(expect = false, update = true)
                else -> {}
            }
        }.launchIn(testScope)

        val clientBJob = clientB.eventsFlow.onEach { event ->
            when (event) {
                is Relay.Model.Event.OnConnectionOpened<*> -> isClientBReady.compareAndSet(expect = false, update = true)
                else -> {}
            }
        }.launchIn(testScope)

        while (!areBothReady.value) {
            delay(10)
        }

        clientAJob.cancel()
        clientBJob.cancel()
    }
}