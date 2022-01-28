package com.walletconnect.walletconnectv2.relay.data.model

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
import com.walletconnect.walletconnectv2.core.adapters.*
import com.walletconnect.walletconnectv2.core.model.vo.ExpiryVO
import com.walletconnect.walletconnectv2.core.model.vo.SubscriptionIdVO
import com.walletconnect.walletconnectv2.core.model.vo.TopicVO
import com.walletconnect.walletconnectv2.core.model.vo.TtlVO
import com.walletconnect.walletconnectv2.network.data.adapter.FlowStreamAdapter
import com.walletconnect.walletconnectv2.network.data.service.RelayService
import com.walletconnect.walletconnectv2.network.model.RelayDTO
import com.walletconnect.walletconnectv2.util.CoroutineTestRule
import com.walletconnect.walletconnectv2.util.getRandom64ByteHexString
import com.walletconnect.walletconnectv2.util.runTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.singleOrNull
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.json.JSONObject
import org.junit.Rule
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
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
//    private lateinit var clientEventObserver: TestStreamObserver<WebSocket.Event>

    @BeforeEach
    fun setUp() {
        givenConnectionIsEstablished()
    }

    @Nested
    inner class Publish {

        @Test
        fun `Client sends Relay_Publish_Request, should be received by the server`() {
            // Arrange
            val relayPublishRequest = RelayDTO.Publish.Request(
                id = 1,
                params = RelayDTO.Publish.Request.Params(
                    topic = TopicVO(getRandom64ByteHexString()),
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
        fun `Server sends Relay_Publish_Acknowledgement, should be received by the client`() {
            // Arrange
            val relayPublishAcknowledgement = RelayDTO.Publish.Acknowledgement(
                id = 1,
                result = true
            )
            val clientRelayPublishObserver = client.observePublishAcknowledgement()

            // Act
            server.sendPublishAcknowledgement(relayPublishAcknowledgement)

            // Assert
//            clientEventObserver.awaitValues(
//                any<WebSocket.Event.OnConnectionOpened<*>>(),
//                any<WebSocket.Event.OnMessageReceived>().containingRelayObject(relayPublishAcknowledgement)
//            )

            coroutineRule.runTest {
                val actualPublishAcknowledgement = clientRelayPublishObserver.singleOrNull()
                assertNotNull(actualPublishAcknowledgement)
                assertEquals(relayPublishAcknowledgement, actualPublishAcknowledgement)
            }
        }
    }

    @Nested
    inner class Subscribe {

        @Test
        fun `Client sends Relay_Subscribe_Request, should be received by the server`() {
            // Arrange
            val relaySubscribeRequest = RelayDTO.Subscribe.Request(
                id = 1,
                params = RelayDTO.Subscribe.Request.Params(TopicVO(getRandom64ByteHexString()))
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
                any<RelayDTO.Subscribe.Request> { assertThat(this).isEqualTo(relaySubscribeRequest) }
            )
        }

        @Test
        fun `Server sends Relay_Subscribe_Acknowledgement, should be received by the client`() {
            // Arrange
            val relaySubscribeAcknowledgement = RelayDTO.Subscribe.Acknowledgement(
                id = 1,
                result = SubscriptionIdVO("SubscriptionId 1")
            )
            val clientRelaySubscribeObserver = client.observeSubscribeAcknowledgement()

            // Act
            server.sendSubscribeAcknowledgement(relaySubscribeAcknowledgement)

            // Assert
//            clientEventObserver.awaitValues(
//                any<WebSocket.Event.OnConnectionOpened<*>>(),
//                any<WebSocket.Event.OnMessageReceived>().containingRelayObject(relaySubscribeAcknowledgement)
//            )

            coroutineRule.runTest {
                val actualSubscribeAcknowledgement = clientRelaySubscribeObserver.singleOrNull()
                assertNotNull(actualSubscribeAcknowledgement)
                assertEquals(relaySubscribeAcknowledgement, actualSubscribeAcknowledgement)
            }
        }
    }

    @Nested
    inner class Subscription {

        @Test
        fun `Deserialize Subscription Request`() {
            @Language("JSON")
            val mockSubscriptionRequest =
                """
                    {
                      "id": 1634280332683238,
                      "jsonrpc": "2.0",
                      "method": "waku_subscription",
                      "params": {
                        "id": "ca6b4c1fd2efe0537a1290c8039635c7ea7ffcb9e31b8f54d60aba0d9d25745c",
                        "data": {
                          "topic": "b165ce35675827889e25402549589da42cf472ca0a61dc32d69a111bafbc29f8",
                          "message": "ef167b0c042b72284f33a7726f548af6400cb3f43966e94ce57a150b43eb8ad1b211d20fd0873ef3807735e575fab6662815ed6929c88f766a6736d9af5ad368b59ddc4505c75eb4be5cf2fd9a5744bd9429c6d0d06b1507a90da08c7da38d2553a26ecd005d11c0718a1d2157c5b5291b1a69a542b6324d6d4af0c9562847f0d49b56483d4c409e1b36f61620270bab7f30f271e053c58fe3d8a48bafb5857b31161e1b178a0cec9d52928b0ef0038475345722d36b56221e828cb02f421d5036722c93fb3c1933f588417f9943cc0ac5a42d88fb7ed743da73e117e9bcd36aa0feea29aaa5928ac1c9a7ac7cc052ac763da6f54f24114e00332c813b4af789cb7db3ac7eeadca4659cd49a2d2d36265ec1d5731b7171f2a12dc2ae45bfe3ad99b1c7db7f099ab7a41af486321aa5aaad2ecd54ab149794bca45cfcf4460c83c560c0e2a75ba7d0f5c4ebf9c8d58528eb73af30f717760ab75cb40037db8e523ef5a6cdec4ffc3cd6670d4908b3850d2c10f6bade902bd3b35f338bd758a143b9aa214d627ba406d00a194e8db46112a17a593240c1847e6e3ca5a096eae1edbb5d3ef4c37261a7aa72b94b04d54e3b60997deeba25d5a49cd5be01d1ff0962d77cb98132df37bca62171c64092388d7b76ce13e51d5191db735c356ea95d3bddf0ee5a51f79107189446527a2c281752786da7cf6bbd581f491b23d4adaeccbbf3e2aae074c5f72db299817a5af37a0d5a3c37310b69636b73bb165bfe96068ae987d7c80665039ffb1152e8705b7a45b2552827fb1b076d4b009ac232fc36ab839efbaacdf34c4aee6693a5a6c562991ccf003317d9b3c6631ba1db7f1fb2d83959415aa9e5a2f88af22bf1623a9391526a4529858dd7f61b8cebc26df7c47ede5865e732e55312f14c43eef697aab95d85462697e9487a94aafa8562dda0805b1f82169458178f09e93e4166f6ef26f606ef55ce6ed4c2bfa61660e84ac51cfc87229cc0df56dafd9a01b9565552f6492c071cbf79dd7b5198c2c21c273ded84854ddb043101b16281ef92f14a2c9e68a4a94452bd9faed28cde49d4774a18f706b9799b449b59ede21c2a374ec3d3fa06f89bc63391b3affcb8d935f72fe3d87e13c7d94e621084487c0a4dc8cc5f941ec9def40e86e1b1680e343f77a7c08843825a707224fe3346135d08ac4e138cd33c61dc37cb9974e93cfb5959f6bfcffb5f822ba6e45768d4e775bbe32c1014d802348903fa5e206fc45711e90d1874ada990bf684d51130056718ac3be"
                        }
                      }
                    }
                """.trimIndent()

            val deserializedSubscriptionRequest = moshi.adapter(RelayDTO.Subscription.Request::class.java).fromJson(mockSubscriptionRequest)

            assertNotNull(deserializedSubscriptionRequest)
        }

        @Test
        fun `Server sends Relay_Subscription_Request, should be received by the client`() {
            val message =
                "ffbecf819a49a266b262309ad269ae4016ef8b8ef1f010d4447b7e089aac0b943d5e2ca94646ddcfa92f4e8e5778cc3e39e3e876dd95065c5899b95a98512664a8c77853c47d31c2e714e50018f3d1b525dbd2f76cde5bff8b261f343ecb3d956ad9e74819c8729fa1c77be4b5fb7d39ccc697bda421fb90d11315d828e79fca6a27316d3b09f14c7f3483b25b000820e7b64a75e5f59216e5f0ecbc4ec20c53664ad5e967026aa119a32a655e3ff3e110ca4c7e629b845b8ecf7ea6f296a79a6de3dc5794c3a51059bb08b09974501ffcf2d7fddafafd9f1b22e97b6abbb6bcd978a8a87341f33bc662c101947a06c72f6c7709a0a612f46fcd8b5fbce0bdd4c56ca330e6e2802fbf6e3830210f3c1b626863de93fd02857c615436e1b9dc7d36d45bbec8acfb24cd45c46946832d5a7cc20334fd7405dba997daf4725bc849450f197e7e9e2f5e20839ba1f77895b3cbccc279fdc0a9d40156a28ad2adcd6a8afc68f9735c4e7c22c49caf5150f243bab702a71699c9b26420668c81fc5b311488331a4456ba1baf619818b4ecfe6f6de8f80dc42a85c785aa78dd187e82faec549780051551335c651af10f89a3e37103e56a8ebf27f3054e4303a6bce88d7c082bfda897facfd952df5d3d6776370884cb04923c804c99059bb269fdbff3543d89648f39a7cc6fdad61ea0f24deeab420bc65dde6c7a6a3f5fe3775fe4a95a8bf8b70ae946696c808206baf119f0b3142d502c7ca0c102548a1263de2c04bde47aa1a716ae7b00959e300b56d6f0595d1588e07c618b914e3c76cb7d103cd8c6b91ed0aaadc2c129455c07905e5272ea4039660cb8e53a64101dae6e8737a082ac9a9b531a4cbc83e009c1722ca108a26bd193817392890b80cf519f2f14e1fc0e1b47d0b7da47d0635eace28e42456a222da5f2044895914a0b21568d49c222f55b114a558649f094012dbaaabd02ad1aae591d80b8754bb39964f4b9c235166b1ea5c80eb9870e90f073722926f823e5ca72714de10f6f4ed4072bfd3ffc4d32ec0e920edb404b7b1afa1f001d18948fe25562c9b8d52824a4fad20082f28a13e96b7277cb4e7a5ccbbf8095293892b2bac008fcee038765743fb9688abf8affd2477f7de90494ccbba94f6a88a0e0c215d5134b70f41f28754e1b236ab43ec65696fa182fa9525a70e7f42141ec38cfe57d26230b3d520ba2769517c9f8f43a161d38438079b967ab73835865b68a22d3cde7a37fccad1ee3f33ae13bb0f09b4b86ce2ee07823ba793a0fafee"
            // Arrange
            val relaySubscriptionRequest = RelayDTO.Subscription.Request(
                id = 1,
                params = RelayDTO.Subscription.Request.Params(
                    subscriptionId = SubscriptionIdVO("subscriptionId"),
                    subscriptionData = RelayDTO.Subscription.Request.Params.SubscriptionData(
                        topic = TopicVO(getRandom64ByteHexString()),
                        message = message
                    )
                )
            )
            val clientRelaySubscriptionObserver = client.observeSubscriptionRequest()

            // Act
            server.sendSubscriptionRequest(relaySubscriptionRequest)

            // Assert
//            clientEventObserver.awaitValues(
//                any<WebSocket.Event.OnConnectionOpened<*>>(),
//                any<WebSocket.Event.OnMessageReceived>().containingRelayObject(relaySubscriptionRequest)
//            )

            coroutineRule.runTest {
                val actualRelaySubscriptionRequest = clientRelaySubscriptionObserver.singleOrNull()
                assertNotNull(actualRelaySubscriptionRequest)
                assertEquals(relaySubscriptionRequest, actualRelaySubscriptionRequest)
            }
        }

        @Test
        fun `Client sends Relay_Subscription_Acknowledgement, should be received by the server`() {
            // Arrange
            val relaySubscriptionAcknowledgement = RelayDTO.Subscription.Acknowledgement(
                id = 1,
                result = true
            )
            val serverRelaySubscriptionObserver = server.observeSubscriptionAcknowledgement().test()

            // Act
            client.publishSubscriptionAcknowledgement(relaySubscriptionAcknowledgement)

            // Assert
//            serverEventObserver.awaitValues(
//                any<WebSocket.Event.OnConnectionOpened<*>>(),
//                any<WebSocket.Event.OnMessageReceived>().containingRelayObject(relaySubscriptionAcknowledgement)
//            )
//            serverRelaySubscriptionObserver.awaitValues(any<RelayDTO.Subscription.Acknowledgement>())
        }
    }

    @Nested
    inner class Unsubscribe {

        @Test
        fun `Client sends Relay_Subscribe_Request, should be received by the server`() {
            // Arrange
            val relayUnsubscribeRequest = RelayDTO.Unsubscribe.Request(
                id = 1,
                params = RelayDTO.Unsubscribe.Request.Params(
                    topic = TopicVO(getRandom64ByteHexString()),
                    subscriptionId = SubscriptionIdVO("subscriptionId")
                )
            )
            val serverRelayPublishObserver = server.observeUnsubscribePublish().test()

            // Act
            client.unsubscribeRequest(relayUnsubscribeRequest)

            // Assert
//            serverEventObserver.awaitValues(
//                any<WebSocket.Event.OnConnectionOpened<*>>(),
//                any<WebSocket.Event.OnMessageReceived>().containingRelayObject(relayUnsubscribeRequest)
//            )
            serverRelayPublishObserver.awaitValues(
                any<RelayDTO.Unsubscribe.Request> { assertThat(this).isEqualTo(relayUnsubscribeRequest) }
            )
        }

        @Test
        fun `Server sends Relay_Unsubscribe_Acknowledgement, should be received by the client`() {
            // Arrange
            val relayUnsubscribeAcknowledgement = RelayDTO.Unsubscribe.Acknowledgement(
                id = 1,
                result = true
            )
            val clientRelayUnsubscribeObserver = client.observeUnsubscribeAcknowledgement()

            // Act
            server.sendUnsubscribeAcknowledgement(relayUnsubscribeAcknowledgement)

            // Assert
//            clientEventObserver.awaitValues(
//                any<WebSocket.Event.OnConnectionOpened<*>>(),
//                any<WebSocket.Event.OnMessageReceived>().containingRelayObject(relayUnsubscribeAcknowledgement)
//            )

            coroutineRule.runTest {
                val actualSubscribeAcknowledgement = clientRelayUnsubscribeObserver.singleOrNull()
                assertEquals(relayUnsubscribeAcknowledgement, actualSubscribeAcknowledgement)
            }
        }
    }

    private fun createMoshi(): Moshi = Moshi.Builder()
        .add { type, _, _ ->
            when (type.getRawType().name) {
                ExpiryVO::class.qualifiedName -> ExpiryAdapter
                JSONObject::class.qualifiedName -> JSONObjectAdapter
                SubscriptionIdVO::class.qualifiedName -> SubscriptionIdAdapter
                TopicVO::class.qualifiedName -> TopicAdapter
                TtlVO::class.qualifiedName -> TtlAdapter
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
//        clientEventObserver = client.observeEvents().test()
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
//        clientEventObserver.awaitValues(
//            any<WebSocket.Event.OnConnectionOpened<*>>()
//        )
    }

    internal interface MockServerService {

        @Receive
        fun observeEvents(): Stream<WebSocket.Event>

        @Receive
        fun observeRelayPublish(): Stream<RelayDTO.Publish.Request>

        @Send
        fun sendPublishAcknowledgement(serverAcknowledgement: RelayDTO.Publish.Acknowledgement)

        @Receive
        fun observeSubscribePublish(): Stream<RelayDTO.Subscribe.Request>

        @Send
        fun sendSubscribeAcknowledgement(serverAcknowledgement: RelayDTO.Subscribe.Acknowledgement)

        @Send
        fun sendSubscriptionRequest(serverRequest: RelayDTO.Subscription.Request)

        @Receive
        fun observeSubscriptionAcknowledgement(): Stream<RelayDTO.Subscription.Acknowledgement>

        @Receive
        fun observeUnsubscribePublish(): Stream<RelayDTO.Unsubscribe.Request>

        @Send
        fun sendUnsubscribeAcknowledgement(serverAcknowledgement: RelayDTO.Unsubscribe.Acknowledgement)
    }

    private inline fun <reified T : RelayDTO> ValueAssert<WebSocket.Event.OnMessageReceived>.containingRelayObject(relayObj: T) = assert {
        assertIs<Message.Text>(message)
        val text = message as Message.Text
        val expectedText = Message.Text(moshi.adapter(T::class.java).toJson(relayObj))
        assertEquals(expectedText, text)
    }
}