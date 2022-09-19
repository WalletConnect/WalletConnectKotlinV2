package com.walletconnect.foundation

import com.walletconnect.foundation.crypto.data.repository.JwtRepository
import com.walletconnect.foundation.di.FoundationDITags
import com.walletconnect.foundation.di.commonModule
import com.walletconnect.foundation.di.cryptoModule
import com.walletconnect.foundation.di.networkModule
import com.walletconnect.foundation.network.BaseRelayClient
import com.walletconnect.foundation.network.RelayInterface
import com.walletconnect.foundation.network.model.Relay
import com.walletconnect.util.addUserAgent
import com.walletconnect.util.bytesToHex
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.junit.jupiter.api.Test
import org.koin.core.KoinApplication
import org.koin.core.qualifier.named
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime


sealed class TestState {
    object Idle : TestState()
    object Success : TestState()
    data class Error(val message: String) : TestState()
}

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
        val testState = MutableStateFlow<TestState>(TestState.Idle)
        val testTopic = Random.nextBytes(32).bytesToHex()
        val testMessage = "testMessage"
        val (clientA: RelayInterface, clientB: RelayInterface) = initTwoClients()

        // Listen to incoming messages/requests
        clientB.subscriptionRequest.onEach {
            println("ClientB subscriptionRequest: $it")
            assertEquals(testMessage, it.params.subscriptionData.message)
            testState.compareAndSet(expect = TestState.Idle, update = TestState.Success)
        }.launchIn(testScope)

        //Await connection
        measureAwaitingForConnection(clientA, clientB)

        //Subscribe to topic
        clientB.subscribe(testTopic) { result ->
            result.fold(
                onSuccess = { println("ClientB subscribe on topic: $testTopic") },
                onFailure = { error ->
                    testState.compareAndSet(
                        expect = TestState.Idle,
                        update = TestState.Error("ClientB failed to subscribe on topic: $testTopic. Message: ${error.message}")
                    )
                }
            )
        }

        //Publish message
        clientA.publish(testTopic, testMessage, Relay.Model.IrnParams(1114, 300)) { result ->
            result.fold(
                onSuccess = { println("ClientA publish on topic: $testTopic") },
                onFailure = { error ->
                    testState.compareAndSet(
                        expect = TestState.Idle,
                        update = TestState.Error("ClientA failed to publish on topic: $testTopic. Message: ${error.message}")
                    )
                }
            )
        }

        //Lock until is finished or timed out
        runBlocking {
            val start = System.currentTimeMillis()
            // Await test finish or check if timeout occurred
            while (testState.value is TestState.Idle && !didTimeout(start, 5000L)) {
                delay(10)
            }

            // Success or fail or idle
            when (testState.value) {
                is TestState.Success -> return@runBlocking
                is TestState.Error -> fail((testState.value as TestState.Error).message)
                is TestState.Idle -> fail("Test timeout")
            }
        }
    }


    @ExperimentalTime
    @Test
    fun `One client sends unencrypted message with too small ttl and receives error from relay`() {
        val testState = MutableStateFlow<TestState>(TestState.Idle)
        val testTopic = Random.nextBytes(32).bytesToHex()
        val testMessage = "testMessage"
        val ttl: Long = 1
        val (clientA: RelayInterface, clientB: RelayInterface) = initTwoClients()

        //Await connection
        measureAwaitingForConnection(clientA, clientB)

        //Publish message
        clientA.publish(testTopic, testMessage, Relay.Model.IrnParams(1114, ttl)) { result ->
            result.fold(
                onSuccess = {
                    testState.compareAndSet(expect = TestState.Idle, update = TestState.Error("ClientA publish on topic: $testTopic with ttl: $ttl"))
                },
                onFailure = {
                    testState.compareAndSet(expect = TestState.Idle, update = TestState.Success)
                }
            )
        }

        //Lock until is finished or timed out
        runBlocking {
            val start = System.currentTimeMillis()
            // Await test finish or check if timeout occurred
            while (testState.value is TestState.Idle && !didTimeout(start, 5000L)) {
                delay(10)
            }

            // Success or fail or idle
            when (testState.value) {
                is TestState.Success -> return@runBlocking
                is TestState.Error -> fail((testState.value as TestState.Error).message)
                is TestState.Idle -> fail("Test timeout")
            }
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

        val clientA: BaseRelayClient = koinAppA.koin.get()
        val clientB: BaseRelayClient = koinAppB.koin.get()

        clientA.relayService = koinAppA.koin.get(named(FoundationDITags.RELAY_SERVICE))
        clientB.relayService = koinAppB.koin.get(named(FoundationDITags.RELAY_SERVICE))

        startLoggingClientEventsFlow(clientA, "ClientA")
        startLoggingClientEventsFlow(clientB, "ClientB")

        return (clientA to clientB)
    }

    private fun didTimeout(start: Long, timeout: Long): Boolean = System.currentTimeMillis() - start > timeout

    private fun awaitConnection(clientA: RelayInterface, clientB: RelayInterface) = runBlocking {
        val isClientAReady = MutableStateFlow(false)
        val isClientBReady = MutableStateFlow(false)
        val areBothReady: StateFlow<Boolean> =
            combine(isClientAReady, isClientBReady) { clientA: Boolean, clientB: Boolean -> clientA && clientB }
                .stateIn(testScope, SharingStarted.Eagerly, false)

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