package org.walletconnect.walletconnectv2.relay;

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.tinder.scarlet.Message
import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.Stream
import com.tinder.scarlet.WebSocket
import com.tinder.scarlet.messageadapter.moshi.MoshiMessageAdapter
import com.tinder.scarlet.testutils.TestStreamObserver
import com.tinder.scarlet.testutils.ValueAssert
import com.tinder.scarlet.testutils.any
import com.tinder.scarlet.testutils.test
import com.tinder.scarlet.utils.getRawType
import com.tinder.scarlet.websocket.mockwebserver.newWebSocketFactory
import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import com.tinder.scarlet.ws.Receive
import com.tinder.scarlet.ws.Send
import com.tinder.streamadapter.coroutines.CoroutinesStreamAdapterFactory
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.Rule
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.walletconnect.walletconnectv2.common.*
import org.walletconnect.walletconnectv2.common.network.adapters.*
import org.walletconnect.walletconnectv2.getRandom64ByteString
import org.walletconnect.walletconnectv2.outofband.client.ClientTypes
import org.walletconnect.walletconnectv2.relay.data.RelayService
import org.walletconnect.walletconnectv2.relay.data.model.Relay
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class RelayTest {

    @Rule
    val mockWebServer = MockWebServer()
    private val serverUrlString by lazy { mockWebServer.url("/").toString() }

    private lateinit var server: MockServerService
    private lateinit var serverEventObserver: TestStreamObserver<WebSocket.Event>

    private lateinit var client: RelayService
    private lateinit var clientEventObserver: TestStreamObserver<WebSocket.Event>

    @BeforeEach
    fun setUp() {
        givenConnectionIsEstablished()
    }

    @Nested
    inner class Publish {

        @Test
        fun `Client sends Relay_Publish_Request, should be received by the server`() {
            // Arrange
            val pairingParams = ClientTypes.PairParams("wc:0b1a3d6c0336662dddb6278ee0aa25380569b79e7e86cfe39fb20b4b189096a0@2?controller=false&publicKey=66db1bd5fad65392d1d5a4856d0d549d2fca9194327138b41c289b961d147860&relay=%7B%22protocol%22%3A%22waku%22%7D")
            val pairingProposal = pairingParams.uri.toPairProposal()
            val preSettlementPairingApprove = pairingProposal.toApprove(1)
            val relayPublishRequest = preSettlementPairingApprove.toRelayPublishRequest(2, Topic(getRandom64ByteString()), createMoshi())
            val serverRelayPublishObserver = server.observeRelayPublish().test()

            // Act
            client.publishRequest(relayPublishRequest)

            // Assert
            serverEventObserver.awaitValues(
                any<WebSocket.Event.OnConnectionOpened<*>>(),
                any<WebSocket.Event.OnMessageReceived>().containingRelayObject(relayPublishRequest)
            )
            serverRelayPublishObserver.awaitValues(
                any<Relay.Publish.Request> { assertThat(this).isEqualTo(relayPublishRequest) }
            )
        }

        @Test
        fun `Server sends Relay_Publish_Response, should be received by the client`() {
            // Arrange
            val relayPublishResponse = Relay.Publish.Response(
                id = 1,
                result = true
            )
            val clientRelayPublishObserver = client.observePublishResponse()

            // Act
            server.sendPublishResponse(relayPublishResponse)

            // Assert
            clientEventObserver.awaitValues(
                any<WebSocket.Event.OnConnectionOpened<*>>(),
                any<WebSocket.Event.OnMessageReceived>().containingRelayObject(relayPublishResponse)
            )

            runBlocking {
                val actualPublishResponse = clientRelayPublishObserver.receiveCatching().getOrNull()
                assertEquals(relayPublishResponse.id, actualPublishResponse?.id)
                assertEquals(relayPublishResponse.jsonrpc, actualPublishResponse?.jsonrpc)
                assertEquals(relayPublishResponse.result, actualPublishResponse?.result)
            }
        }
    }

    @Nested
    inner class Subscribe {

        @Test
        fun `Client sends Relay_Subscribe_Request, should be received by the server`() {
            // Arrange
            val relaySubscribeRequest = Relay.Subscribe.Request(
                id = 1,
                params = Relay.Subscribe.Request.Params(Topic(getRandom64ByteString()))
            )
            val serverRelayPublishObserver = server.observeSubscribePublish().test()

            // Act
            client.subscribeRequest(relaySubscribeRequest)

            // Assert
            serverEventObserver.awaitValues(
                any<WebSocket.Event.OnConnectionOpened<*>>(),
                any<WebSocket.Event.OnMessageReceived>().containingRelayObject(relaySubscribeRequest)
            )
            serverRelayPublishObserver.awaitValues(
                any<Relay.Subscribe.Request> { assertThat(this).isEqualTo(relaySubscribeRequest) }
            )
        }

        @Test
        fun `Server sends Relay_Subscribe_Response, should be received by the client`() {
            // Arrange
            val relaySubscribeResponse = Relay.Subscribe.Response(
                id = 1,
                result = SubscriptionId("SubscriptionId 1")
            )
            val clientRelaySubscribeObserver = client.observeSubscribeResponse()

            // Act
            server.sendSubscribeResponse(relaySubscribeResponse)

            // Assert
            clientEventObserver.awaitValues(
                any<WebSocket.Event.OnConnectionOpened<*>>(),
                any<WebSocket.Event.OnMessageReceived>().containingRelayObject(relaySubscribeResponse)
            )

            runBlocking {
                val actualSubscribeResponse = clientRelaySubscribeObserver.receiveCatching().getOrNull()
                assertEquals(relaySubscribeResponse, actualSubscribeResponse)
            }
        }
    }

    @Nested
    inner class Subscription {

        @Test
        fun `Server sends Relay_Subscription_Request, should be received by the client`() {
            // Arrange
            val relaySubscriptionRequest = Relay.Subscription.Request(
                id = 1,
                params = Relay.Subscription.Request.Params(
                    subscriptionId = SubscriptionId("subscriptionId"),
                    data = Relay.Subscription.Request.Params.SubscriptionData(
                        topic = Topic(getRandom64ByteString()),
                        message = "This is a test"
                    )
                )
            )
            val clientRelaySubscriptionObserver = client.observeSubscriptionRequest()

            // Act
            server.sendSubscriptionRequest(relaySubscriptionRequest)

            // Assert
            clientEventObserver.awaitValues(
                any<WebSocket.Event.OnConnectionOpened<*>>(),
                any<WebSocket.Event.OnMessageReceived>().containingRelayObject(relaySubscriptionRequest)
            )

            runBlocking {
                assertEquals(relaySubscriptionRequest, clientRelaySubscriptionObserver.receiveCatching().getOrNull())
            }
        }

        @Test
        fun `Client sends Relay_Subscription_Response, should be received by the server`() {
            // Arrange
            val relaySubscriptionResponse = Relay.Subscription.Response(
                id = 1,
                result = true
            )
            val serverRelaySubscriptionObserver = server.observeSubscriptionResponse().test()

            // Act
            client.subscriptionResponse(relaySubscriptionResponse)

            // Assert
            serverEventObserver.awaitValues(
                any<WebSocket.Event.OnConnectionOpened<*>>(),
                any<WebSocket.Event.OnMessageReceived>().containingRelayObject(relaySubscriptionResponse)
            )
            serverRelaySubscriptionObserver.awaitValues(
                any<Relay.Subscription.Response> { assertThat(this).isEqualTo(relaySubscriptionResponse) }
            )
        }
    }

    @Nested
    inner class Unsubscribe {

        @Test
        fun `Client sends Relay_Subscribe_Request, should be received by the server`() {
            // Arrange
            val relayUnsubscribeRequest = Relay.Unsubscribe.Request(
                id = 1,
                params = Relay.Unsubscribe.Request.Params(
                    topic = Topic(getRandom64ByteString()),
                    subscriptionId = SubscriptionId("subscriptionId")
                )
            )
            val serverRelayPublishObserver = server.observeUnsubscribePublish().test()

            // Act
            client.unsubscribeRequest(relayUnsubscribeRequest)

            // Assert
            serverEventObserver.awaitValues(
                any<WebSocket.Event.OnConnectionOpened<*>>(),
                any<WebSocket.Event.OnMessageReceived>().containingRelayObject(relayUnsubscribeRequest)
            )
            serverRelayPublishObserver.awaitValues(
                any<Relay.Unsubscribe.Request> { assertThat(this).isEqualTo(relayUnsubscribeRequest) }
            )
        }

        @Test
        fun `Server sends Relay_Unsubscribe_Response, should be received by the client`() {
            // Arrange
            val relayUnsubscribeResponse = Relay.Unsubscribe.Response(
                id = 1,
                result = true
            )
            val clientRelayUnsubscribeObserver = client.observeUnsubscribeResponse()

            // Act
            server.sendUnsubscribeResponse(relayUnsubscribeResponse)

            // Assert
            clientEventObserver.awaitValues(
                any<WebSocket.Event.OnConnectionOpened<*>>(),
                any<WebSocket.Event.OnMessageReceived>().containingRelayObject(relayUnsubscribeResponse)
            )

            runBlocking {
                val actualSubscribeResponse = clientRelayUnsubscribeObserver.receiveCatching().getOrNull()
                assertEquals(relayUnsubscribeResponse, actualSubscribeResponse)
            }
        }
    }

    private fun givenConnectionIsEstablished() {
        createClientAndServer()
        blockUntilConnectionIsEstablish()
    }

    private fun createClientAndServer() {
        server = createServer()
        serverEventObserver = server.observeEvents().test()
        client = createClient()
        clientEventObserver = client.observeEvents().test()
    }

    private fun createMoshi(): Moshi = Moshi.Builder()
        .addLast { type, _, _ ->
            when(type.getRawType().name) {
                Expiry::class.qualifiedName -> ExpiryAdapter
                JSONObject::class.qualifiedName -> JSONObjectAdapter
                SubscriptionId::class.qualifiedName -> SubscriptionIdAdapter
                Topic::class.qualifiedName -> TopicAdapter
                Ttl::class.qualifiedName -> TtlAdapter
                else -> null
            }
        }
        .add(KotlinJsonAdapterFactory())
        .build()

    private fun createServer(): MockServerService = Scarlet.Builder()
        .webSocketFactory(mockWebServer.newWebSocketFactory())
        .addMessageAdapterFactory(MoshiMessageAdapter.Factory(moshi = createMoshi()))
        .build()
        .create()

    private fun createClient(): RelayService = Scarlet.Builder()
        .webSocketFactory(createOkHttpClient().newWebSocketFactory(serverUrlString))
        .addStreamAdapterFactory(CoroutinesStreamAdapterFactory())
        .addMessageAdapterFactory(MoshiMessageAdapter.Factory(moshi = createMoshi()))
        .build().create()

    private fun createOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .writeTimeout(500, TimeUnit.MILLISECONDS)
        .readTimeout(500, TimeUnit.MILLISECONDS)
        .build()

    private fun blockUntilConnectionIsEstablish() {
        serverEventObserver.awaitValues(
            any<WebSocket.Event.OnConnectionOpened<*>>()
        )
        clientEventObserver.awaitValues(
            any<WebSocket.Event.OnConnectionOpened<*>>()
        )
    }

    internal interface MockServerService {

        @Receive
        fun observeEvents(): Stream<WebSocket.Event>

        @Receive
        fun observeRelayPublish(): Stream<Relay.Publish.Request>

        @Send
        fun sendPublishResponse(serverResponse: Relay.Publish.Response)

        @Receive
        fun observeSubscribePublish(): Stream<Relay.Subscribe.Request>

        @Send
        fun sendSubscribeResponse(serverResponse: Relay.Subscribe.Response)

        @Send
        fun sendSubscriptionRequest(serverRequest: Relay.Subscription.Request)

        @Receive
        fun observeSubscriptionResponse(): Stream<Relay.Subscription.Response>

        @Receive
        fun observeUnsubscribePublish(): Stream<Relay.Unsubscribe.Request>

        @Send
        fun sendUnsubscribeResponse(serverResponse: Relay.Unsubscribe.Response)
    }

    private inline fun <reified T: Relay> ValueAssert<WebSocket.Event.OnMessageReceived>.containingRelayObject(relayObj: T) = assert {
        assertIs<Message.Text>(message)
        val text = message as Message.Text
        val expectedText = Message.Text(createMoshi().adapter(T::class.java).toJson(relayObj))
        assertEquals(expectedText, text)
    }
}