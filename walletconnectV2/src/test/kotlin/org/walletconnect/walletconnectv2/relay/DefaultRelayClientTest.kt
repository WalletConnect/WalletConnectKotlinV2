package org.walletconnect.walletconnectv2.relay

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.tinder.scarlet.Lifecycle
import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.Stream
import com.tinder.scarlet.WebSocket
import com.tinder.scarlet.lifecycle.LifecycleRegistry
import com.tinder.scarlet.messageadapter.moshi.MoshiMessageAdapter
import com.tinder.scarlet.testutils.*
import com.tinder.scarlet.websocket.mockwebserver.newWebSocketFactory
import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import com.tinder.scarlet.ws.Receive
import com.tinder.scarlet.ws.Send
import com.tinder.streamadapter.coroutines.CoroutinesStreamAdapterFactory
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.jupiter.api.Test
import org.walletconnect.walletconnectv2.common.Topic
import org.walletconnect.walletconnectv2.common.network.adapters.ExpiryAdapter
import org.walletconnect.walletconnectv2.common.network.adapters.JSONObjectAdapter
import org.walletconnect.walletconnectv2.common.network.adapters.TopicAdapter
import org.walletconnect.walletconnectv2.common.network.adapters.TtlAdapter
import org.walletconnect.walletconnectv2.common.toApprove
import org.walletconnect.walletconnectv2.common.toPairProposal
import org.walletconnect.walletconnectv2.common.toRelayPublishRequest
import org.walletconnect.walletconnectv2.getRandom64ByteString
import org.walletconnect.walletconnectv2.outofband.ClientTypes
import org.walletconnect.walletconnectv2.relay.data.RelayService
import org.walletconnect.walletconnectv2.relay.data.model.Relay
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class DefaultRelayClientTest {

    @get:Rule
    val mockWebServer = MockWebServer()
    private val serverUrlString by lazy { mockWebServer.url("/").toString() }
    private val moshi: Moshi = Moshi.Builder()
        .add(TopicAdapter)
        .add(ExpiryAdapter)
        .add(TtlAdapter)
        .add(JSONObjectAdapter)
        .add(KotlinJsonAdapterFactory())
        .build()

    private val serverLifecycleRegistry = LifecycleRegistry()
    private lateinit var mockService: MockServerService
    private lateinit var serverEventObserver: TestStreamObserver<WebSocket.Event>

    private val clientLifecycleRegistry = LifecycleRegistry()
    private lateinit var client: RelayService
    private lateinit var clientEventObserver: TestStreamObserver<WebSocket.Event>

    // Commented code is a work in progress. Test currently only passes when publish is serialized. Will look into sending an object and having mock server verify
    @Test
    fun `Given a DApp wants to connect to WalletConnect, When a Pairing Proposal URI is sent the WC SDK, Then parse the URI and create a Pairing Proposal object`() {
        givenConnectionIsEstablished()
        val mockServerObserveText = mockService.observeText()
        val uriString = ClientTypes.PairParams("wc:0b1a3d6c0336662dddb6278ee0aa25380569b79e7e86cfe39fb20b4b189096a0@2?controller=false&publicKey=66db1bd5fad65392d1d5a4856d0d549d2fca9194327138b41c289b961d147860&relay=%7B%22protocol%22%3A%22waku%22%7D")
        val pairingProposal = uriString.uri.toPairProposal()
        val preSettlementPairingApprove = pairingProposal.toApprove(Random.nextInt())
        val relayPublishRequest = preSettlementPairingApprove.toRelayPublishRequest(Random.nextInt(), Topic(getRandom64ByteString()), moshi)
        val relayPublishRequestJson = moshi.adapter(Relay.Publish.Request::class.java).toJson(relayPublishRequest)

        client.sendText(relayPublishRequestJson)

        serverEventObserver.awaitValues(
            any<WebSocket.Event.OnConnectionOpened<*>>(),
            any<WebSocket.Event.OnMessageReceived>().containingText(relayPublishRequestJson)
        )

        runBlocking {
            assertThat(mockServerObserveText.receiveCatching().getOrNull()).isEqualTo(relayPublishRequestJson)
//            val serverReceivedMessage = mockService.observePublishRequest().receiveCatching().getOrNull()
        }
    }

//    private fun ValueAssert<WebSocket.Event.OnMessageReceived>.containingPublishRequest(publishRequest: Relay.Publish.Request) = assert {
//        assertIs<Message.Text>(message)
//        val text = message as Message.Text
//        val expectedText = Message.Text(moshi.adapter(Relay.Publish.Request::class.java).toJson(publishRequest))
//        assertEquals(expectedText, text)
//    }

    private fun givenConnectionIsEstablished() {
        createClientAndServer()
        serverLifecycleRegistry.onNext(Lifecycle.State.Started)
        clientLifecycleRegistry.onNext(Lifecycle.State.Started)
        blockUntilConnectionIsEstablish()
    }

    private fun createClientAndServer() {
        mockService = createServer()
        serverEventObserver = mockService.observeEvents().test()
        client = createClient()
        clientEventObserver = client.observeEvents().test()
    }

    private fun createServer(): MockServerService {
        val webSocketFactory = mockWebServer.newWebSocketFactory()
        val scarlet = Scarlet.Builder()
            .webSocketFactory(webSocketFactory)
            .lifecycle(serverLifecycleRegistry)
            .addStreamAdapterFactory(CoroutinesStreamAdapterFactory())
            .build()
        return scarlet.create()
    }

    private fun createClient(): RelayService {
        val okHttpClient = OkHttpClient.Builder()
            .writeTimeout(500, TimeUnit.MILLISECONDS)
            .readTimeout(500, TimeUnit.MILLISECONDS)
            .build()
        val webSocketFactory = okHttpClient.newWebSocketFactory(serverUrlString)
        val scarlet = Scarlet.Builder()
            .webSocketFactory(webSocketFactory)
            .lifecycle(clientLifecycleRegistry)
//            .addMessageAdapterFactory(MoshiMessageAdapter.Factory(moshi))
            .addStreamAdapterFactory(CoroutinesStreamAdapterFactory())
            .build()
        return scarlet.create()
    }

    private fun blockUntilConnectionIsEstablish() {
        clientEventObserver.awaitValues(any<WebSocket.Event.OnConnectionOpened<*>>())
        serverEventObserver.awaitValues(any<WebSocket.Event.OnConnectionOpened<*>>())
    }

    private interface MockServerService {

        @Receive
        fun observeEvents(): Stream<WebSocket.Event>

        @Receive
        fun observeText(): ReceiveChannel<String>

        @Send
        fun sendText(message: String)
    }
}