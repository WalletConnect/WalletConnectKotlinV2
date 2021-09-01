package org.walletconnect.walletconnectv2

import com.tinder.scarlet.Lifecycle
import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.Stream
import com.tinder.scarlet.WebSocket
import com.tinder.scarlet.lifecycle.LifecycleRegistry
import com.tinder.scarlet.testutils.*
import com.tinder.scarlet.websocket.mockwebserver.newWebSocketFactory
import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import com.tinder.scarlet.ws.Receive
import com.tinder.streamadapter.coroutines.CoroutinesStreamAdapterFactory
import io.mockk.spyk
import kotlinx.coroutines.channels.ReceiveChannel
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockWebServer
import org.junit.Rule
import org.junit.jupiter.api.*
import org.walletconnect.walletconnectv2.data.RelayService
import java.net.URI
import java.util.concurrent.TimeUnit
import kotlin.test.assertNotNull

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class DefaultWebSocketClientTest {

    @get:Rule
    val mockWebServer = MockWebServer()
    private val serverUrlString by lazy { mockWebServer.url("/").toString() }

    private lateinit var mockService: MockServerService
    private lateinit var serverEventObserver: TestStreamObserver<WebSocket.Event>
    private val serverLifecycleRegistry = LifecycleRegistry()

    private lateinit var client: RelayService
    private lateinit var clientEventObserver: TestStreamObserver<WebSocket.Event>
    private val clientLifecycleRegistry = LifecycleRegistry()

    @BeforeAll
    fun beforeAllSetUp() {
        mockService = buildServer()
        client = spyk(DefaultWebSocketClient.initLocal(false, mockWebServer.port).scarlet).create()
    }

    @AfterAll
    fun afterAllTearDown() {
    }

    @BeforeEach
    fun setUp() {
        serverEventObserver = mockService.observeEvents().test()
        serverLifecycleRegistry.onNext(Lifecycle.State.Started)

        clientEventObserver = client.observeEvents().test()
        clientLifecycleRegistry.onNext(Lifecycle.State.Started)

        serverEventObserver.awaitValues(any<WebSocket.Event.OnConnectionOpened<*>>())
        clientEventObserver.awaitValues(any<WebSocket.Event.OnConnectionOpened<*>>())
    }

    @AfterEach
    fun tearDown() {
    }

//    @Test
//    fun send_givenConnectionIsEstablished_shouldBeReceivedByTheServer() {
//        // Given
//        val textMessage1 = "Hello"
//        val textMessage2 = "Hi!"
//        val bytesMessage1 = "Yo".toByteArray()
//        val bytesMessage2 = "Sup".toByteArray()
//        val testTextChannel = mockService.observeText()
//        val testBytesChannel = mockService.observeBytes()
//
//        // When
//        client.sendText(textMessage1)
//        val isSendTextSuccessful = client.sendTextAndConfirm(textMessage2)
//        client.sendBytes(bytesMessage1)
//        val isSendBytesSuccessful = client.sendBytesAndConfirm(bytesMessage2)
//
//        // Then
//        assertThat(isSendTextSuccessful).isTrue
//        assertThat(isSendBytesSuccessful).isTrue
//
//        serverEventObserver.awaitValues(
//            any<WebSocket.Event.OnConnectionOpened<*>>(),
//            any<WebSocket.Event.OnMessageReceived>().containingText(textMessage1),
//            any<WebSocket.Event.OnMessageReceived>().containingText(textMessage2),
//            any<WebSocket.Event.OnMessageReceived>().containingBytes(bytesMessage1),
//            any<WebSocket.Event.OnMessageReceived>().containingBytes(bytesMessage2)
//        )
//
//        runBlocking {
//            assertThat(testTextChannel.receiveCatching().getOrNull()).isEqualTo(textMessage1)
//            assertThat(testTextChannel.receiveCatching().getOrNull()).isEqualTo(textMessage2)
//
//            assertThat(testBytesChannel.receiveCatching().getOrNull()).isEqualTo(bytesMessage1)
//            assertThat(testBytesChannel.receiveCatching().getOrNull()).isEqualTo(bytesMessage2)
//        }
//    }

    @Test
    fun `Given a DApp wants to connect to WalletConnect, When a Pairing Proposal URI is sent the WC SDK, Then parse the URI and create a Pairing Proposal object`() {
        val uriString = "wc:0b1a3d6c0336662dddb6278ee0aa25380569b79e7e86cfe39fb20b4b189096a0@2?controller=false&publicKey=66db1bd5fad65392d1d5a4856d0d549d2fca9194327138b41c289b961d147860&relay=%7B%22protocol%22%3A%22waku%22%7D"
//        val
//        val pairingUri = URI()
//        assertNotNull(pairingUri.scheme)
//        assertNotNull(pairingUri.userInfo)
//        assertNotNull(pairingUri.host)
    }

//    private interface PracticeService {
//
//        @Receive
//        fun observeEvents(): Stream<WebSocket.Event>
//
//        @Send
//        fun sendText(message: String)
//
//        @Send
//        fun sendTextAndConfirm(message: String): Boolean
//
//        @Send
//        fun sendBytes(message: ByteArray)
//
//        @Send
//        fun sendBytesAndConfirm(message: ByteArray): Boolean
//    }

    private interface MockServerService {

        @Receive
        fun observeEvents(): Stream<WebSocket.Event>

        @Receive
        fun observeText(): ReceiveChannel<String>

        @Receive
        fun observeBytes(): ReceiveChannel<ByteArray>
    }

    private fun buildServer(): MockServerService {
        val webSocketFactory = mockWebServer.newWebSocketFactory()
        val scarlet = Scarlet.Builder()
            .webSocketFactory(webSocketFactory)
            .lifecycle(serverLifecycleRegistry)
            .addStreamAdapterFactory(CoroutinesStreamAdapterFactory())
            .build()
        return scarlet.create()
    }

    private fun buildClient(): RelayService {
        val okHttpClient = OkHttpClient.Builder()
            .writeTimeout(500, TimeUnit.MILLISECONDS)
            .readTimeout(500, TimeUnit.MILLISECONDS)
            .build()
        val webSocketFactory = okHttpClient.newWebSocketFactory(serverUrlString)
        val scarlet = Scarlet.Builder()
            .webSocketFactory(webSocketFactory)
            .lifecycle(clientLifecycleRegistry)
            .addStreamAdapterFactory(CoroutinesStreamAdapterFactory())
            .build()
        return scarlet.create()
    }
}