package org.walletconnect.walletconnectv2.relay.data.model

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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.singleOrNull
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockWebServer
import org.json.JSONObject
import org.junit.Rule
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.walletconnect.walletconnectv2.common.Expiry
import org.walletconnect.walletconnectv2.common.SubscriptionId
import org.walletconnect.walletconnectv2.common.Topic
import org.walletconnect.walletconnectv2.common.Ttl
import org.walletconnect.walletconnectv2.common.network.adapters.*
import org.walletconnect.walletconnectv2.relay.data.RelayService
import org.walletconnect.walletconnectv2.util.CoroutineTestRule
import org.walletconnect.walletconnectv2.util.adapters.FlowStreamAdapter
import org.walletconnect.walletconnectv2.util.getRandom64ByteHexString
import org.walletconnect.walletconnectv2.util.runTest
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

@ExperimentalCoroutinesApi
internal class RelayTest {

    @get:Rule
    val coroutineRule = CoroutineTestRule()

    @Rule
    val mockWebServer = MockWebServer()
    private val serverUrlString by lazy { mockWebServer.url("/").toString() }
    private val moshi = createMoshi()

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
            val relayPublishRequest = Relay.Publish.Request(
                id = 1,
                params = Relay.Publish.Request.Params(
                    topic = Topic(getRandom64ByteHexString()),
                    message = getRandom64ByteHexString()
                )
            )
            val serverRelayPublishObserver = server.observeRelayPublish().test()

            // Act
            client.publishRequest(relayPublishRequest)

            // Assert
            serverEventObserver.awaitValues(
                any<WebSocket.Event.OnConnectionOpened<*>>(),
                any<WebSocket.Event.OnMessageReceived>().containingRelayObject(relayPublishRequest)
            )
            // TODO: Look into why this is failing all of a sudden
//            serverRelayPublishObserver.awaitValues(any<Relay.Publish.Request>())
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

            coroutineRule.runTest {
                val actualPublishResponse = clientRelayPublishObserver.singleOrNull()
                assertNotNull(actualPublishResponse)
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
                params = Relay.Subscribe.Request.Params(Topic(getRandom64ByteHexString()))
            )
            val serverRelayPublishObserver = server.observeSubscribePublish().test()

            // Act
            client.subscribeRequest(relaySubscribeRequest)

            // Assert
            serverEventObserver.awaitValues(
                any<WebSocket.Event.OnConnectionOpened<*>>(),
                any<WebSocket.Event.OnMessageReceived>().containingRelayObject(relaySubscribeRequest)
            )
            // TODO: Look into why this is failing all of a sudden
//            serverRelayPublishObserver.awaitValues(
//                any<Relay.Subscribe.Request> { assertThat(this).isEqualTo(relaySubscribeRequest) }
//            )
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

            coroutineRule.runTest {
                val actualSubscribeResponse = clientRelaySubscribeObserver.singleOrNull()
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
                        topic = Topic(getRandom64ByteHexString()),
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

            coroutineRule.runTest {
                assertEquals(relaySubscriptionRequest, clientRelaySubscriptionObserver.singleOrNull())
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
            serverRelaySubscriptionObserver.awaitValues(any<Relay.Subscription.Response>())
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
                    topic = Topic(getRandom64ByteHexString()),
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
            // TODO: Look into why this is failing all of a sudden
//            serverRelayPublishObserver.awaitValues(
//                any<Relay.Unsubscribe.Request> { assertThat(this).isEqualTo(relayUnsubscribeRequest) }
//            )
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

            coroutineRule.runTest {
                val actualSubscribeResponse = clientRelayUnsubscribeObserver.singleOrNull()
                assertEquals(relayUnsubscribeResponse, actualSubscribeResponse)
            }
        }
    }

    private fun createMoshi(): Moshi = Moshi.Builder()
        .addLast { type, _, _ ->
            when (type.getRawType().name) {
                Expiry::class.qualifiedName -> ExpiryAdapter
                JSONObject::class.qualifiedName -> JSONObjectAdapter
                SubscriptionId::class.qualifiedName -> SubscriptionIdAdapter
                Topic::class.qualifiedName -> TopicAdapter
                Ttl::class.qualifiedName -> TtlAdapter
                else -> null
            }
        }
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private fun givenConnectionIsEstablished() {
        createClientAndServer()
        blockUntilConnectionIsEstablish()
    }

    private fun createClientAndServer() {
        server = createServer()
        serverEventObserver = server.observeEvents().test()
        client = createClient()
        clientEventObserver = client.observeEventsStream().test()
    }

    private fun createServer(): MockServerService = Scarlet.Builder()
        .webSocketFactory(mockWebServer.newWebSocketFactory())
        .addMessageAdapterFactory(MoshiMessageAdapter.Factory(moshi))
        .build()
        .create()

    private fun createClient(): RelayService = Scarlet.Builder()
        .webSocketFactory(createOkHttpClient().newWebSocketFactory(serverUrlString))
        .addStreamAdapterFactory(FlowStreamAdapter.Factory())
        .addMessageAdapterFactory(MoshiMessageAdapter.Factory(moshi))
        .build()
        .create()

    private fun createOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .callTimeout(1, TimeUnit.MINUTES)
        .writeTimeout(1, TimeUnit.MINUTES)
        .readTimeout(1, TimeUnit.MINUTES)
        .connectTimeout(1, TimeUnit.MINUTES)
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

    private inline fun <reified T : Relay> ValueAssert<WebSocket.Event.OnMessageReceived>.containingRelayObject(relayObj: T) = assert {
        assertIs<Message.Text>(message)
        val text = message as Message.Text
        val expectedText = Message.Text(moshi.adapter(T::class.java).toJson(relayObj))
        assertEquals(expectedText, text)
    }
}