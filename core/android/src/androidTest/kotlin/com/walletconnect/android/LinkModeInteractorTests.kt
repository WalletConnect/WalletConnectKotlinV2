package com.walletconnect.android

//import androidx.test.runner.AndroidJUnit4
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.crypto.codec.Codec
import com.walletconnect.android.internal.common.json_rpc.data.JsonRpcSerializer
import com.walletconnect.android.internal.common.json_rpc.domain.link_mode.LinkModeJsonRpcInteractor
import com.walletconnect.android.internal.common.json_rpc.model.JsonRpcHistoryRecord
import com.walletconnect.android.internal.common.model.EnvelopeType
import com.walletconnect.android.internal.common.model.Participants
import com.walletconnect.android.internal.common.model.sync.ClientJsonRpc
import com.walletconnect.android.internal.common.model.type.JsonRpcClientSync
import com.walletconnect.android.internal.common.storage.rpc.JsonRpcHistory
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.foundation.common.model.Topic
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

class LinkModeInteractorTests {
    private val chaChaPolyCodec: Codec = mockk()
    private val jsonRpcHistory: JsonRpcHistory = mockk()
    private val context: Context = ApplicationProvider.getApplicationContext<Context>()
    private val serializer: JsonRpcSerializer = mockk()
    private val interactor: LinkModeJsonRpcInteractor

    init {
        mockkObject(wcKoinApp)

        every { wcKoinApp.koin.get<JsonRpcSerializer>() } returns serializer
        interactor = LinkModeJsonRpcInteractor(chaChaPolyCodec, jsonRpcHistory, context)
    }

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private val payload: JsonRpcClientSync<*> = mockk {
        every { id } returns 1
        every { method } returns "wc_sessionAuthenticate"
    }

    private val clientJsonRpc: ClientJsonRpc = mockk {
        every { id } returns 1
        every { method } returns "wc_sessionAuthenticate"
    }

    private val response: JsonRpcResponse = mockk {
        every { id } returns 1
    }

    private val topic = Topic("test_topic")
    private val appLink = "https://web3modal-laboratory-git-chore-kotlin-assetlinks-walletconnect1.vercel.app/wallet"
    private val envelopeType = EnvelopeType.TWO
    private val requestJson = """
            {"id":1720520264638574,"jsonrpc":"2.0","method":"wc_sessionAuthenticate","params":{"requester":{"publicKey":"242f16c16b035d6f592a1438a37529cc2396bd9d0dee25eb9e94ac8104282a04","metadata":{"description":"Kotlin Dapp Implementation","url":"https://web3modal-laboratory-git-chore-kotlin-assetlinks-walletconnect1.vercel.app","icons":["https://gblobscdn.gitbook.com/spaces%2F-LJJeCjcLrr53DcT1Ml7%2Favatar.png?alt=media"],"name":"Kotlin Dapp","redirect":{"native":"kotlin-dapp-wc://request","universal":"https://web3modal-laboratory-git-chore-kotlin-assetlinks-walletconnect1.vercel.app/dapp","linkMode":true}}},"authPayload":{"type":"eip4361","chains":["eip155:1"],"domain":"sample.kotlin.dapp","aud":"https://web3inbox.com/all-apps","nonce":"44c2ec99fab82d65d7cf7e84","version":"1","iat":"2024-07-09T12:17:44+02:00","statement":"Sign in with wallet.","resources":["urn:recap:eyJhdHQiOnsiaHR0cHM6Ly9ub3RpZnkud2FsbGV0Y29ubmVjdC5jb20vYWxsLWFwcHMiOnsiY3J1ZC9zdWJzY3JpcHRpb25zIjpbe31dLCJjcnVkL25vdGlmaWNhdGlvbnMiOlt7fV19fX0=","ipfs://bafybeiemxf5abjwjbikoz4mc3a3dla6ual3jsgpdr4cjr3oz3evfyavhwq/","urn:recap:eyJhdHQiOnsiZWlwMTU1Ijp7InJlcXVlc3RcL3BlcnNvbmFsX3NpZ24iOlt7fV0sInJlcXVlc3RcL2V0aF9zaWduVHlwZWREYXRhIjpbe31dfSwiaHR0cHM6XC9cL25vdGlmeS53YWxsZXRjb25uZWN0LmNvbVwvYWxsLWFwcHMiOnsiY3J1ZFwvc3Vic2NyaXB0aW9ucyI6W3t9XSwiY3J1ZFwvbm90aWZpY2F0aW9ucyI6W3t9XX19fQ"]},"expiryTimestamp":1720523864}}
        """.trimIndent()
    private val encryptedResponse = "encrypted_response".toByteArray()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testTriggerRequestWithValidData() = testScope.runTest {
        every { serializer.serialize(payload) } returns requestJson
        every { jsonRpcHistory.setRequest(any(), any(), any(), any(), any()) } returns true
        every { chaChaPolyCodec.encrypt(any(), any(), any()) } returns encryptedResponse

        interactor.triggerRequest(payload, topic, appLink, envelopeType)

        verify {
            serializer.serialize(payload)
            chaChaPolyCodec.encrypt(topic, requestJson, envelopeType)
        }
    }

    @Test
    fun testTriggerRequestWithSerializationFailure() = testScope.runTest {
        every { serializer.serialize(payload) } returns null

        try {
            interactor.triggerRequest(payload, topic, appLink, envelopeType)
        } catch (e: IllegalStateException) {
            assert(e.message == "LinkMode: Cannot serialize the request")
        }
    }

    @Test
    fun testTriggerResponseWithValidData() = testScope.runTest {
        val participants: Participants? = null
        val envelopeType = EnvelopeType.ZERO
        val responseJson = "response_json"
        val encryptedResponse = "encrypted_response".toByteArray()
        val jsonRpcRecord: JsonRpcHistoryRecord = mockk()

        every { serializer.serialize(response) } returns responseJson
        every { chaChaPolyCodec.encrypt(any(), any(), any(), any()) } returns encryptedResponse
        every { jsonRpcHistory.updateRequestWithResponse(any(), any()) } returns jsonRpcRecord

        interactor.triggerResponse(topic, response, appLink, participants, envelopeType)

        verify {
            serializer.serialize(response)
            chaChaPolyCodec.encrypt(topic, responseJson, envelopeType, participants)
        }
    }

    @Test
    fun testTriggerResponseWithSerializationFailure() = testScope.runTest {
        val response: JsonRpcResponse = mockk()
        val participants: Participants? = null
        val envelopeType = EnvelopeType.ZERO

        every { serializer.serialize(response) } returns null

        try {
            interactor.triggerResponse(topic, response, appLink, participants, envelopeType)
        } catch (e: IllegalStateException) {
            assert(e.message == "LinkMode: Cannot serialize the response")
        }
    }

    @Test
    fun testDispatchEnvelopeWithValidData() = testScope.runTest {
        val url =
            "https://web3modal-laboratory-git-chore-kotlin-assetlinks-walletconnect1.vercel.app/dapp?wc_ev=AWCUZ9qZjOuuPXoeCspXBMFd8NXumb1ZoXemH4BPoA1L1_bsdepR39jMhAc2u9L9OPyrhrCSDv-KSYI-oRxgwkLSRheWksBoOobFmr2k9yeDTFfPQQA_xVchY2r1d2RUHB30cS2d9yNKI0DUyWYfycd36IIjPLqM-MDiYi4dUV9SKlvaCGHYtCuLL55MlT0ehtIJF8jqmMqmQ9BOlNhiZ3MGtg&topic=c600171ea687023a73a78c5bad2e01fae0497f6af8129a0334d1e3bd5e3030e3"
        val envelope = "decrypted_envelope"

        every { chaChaPolyCodec.decrypt(any(), any()) } returns envelope
        coEvery { serializer.tryDeserialize<ClientJsonRpc>(any()) } returns clientJsonRpc
        coEvery { serializer.tryDeserialize<JsonRpcResponse>(any()) } returns null
        coEvery { serializer.tryDeserialize<JsonRpcResponse.JsonRpcError>(any()) } returns null

        interactor.dispatchEnvelope(url)

        coVerify {
            chaChaPolyCodec.decrypt(Topic("c600171ea687023a73a78c5bad2e01fae0497f6af8129a0334d1e3bd5e3030e3"), any())
            serializer.tryDeserialize<ClientJsonRpc>(envelope)
        }
    }

    @Test
    fun testDispatchEnvelopeWithMissingWc_evParameter() = testScope.runTest {
        val url = "test_url?topic=test_topic"

        try {
            interactor.dispatchEnvelope(url)
        } catch (e: IllegalStateException) {
            assert(e.message == "LinkMode: Missing wc_ev parameter")
        }
    }

    @Test
    fun testDispatchEnvelopeWithMissingTopicParameter() = testScope.runTest {
        val url = "test_url?wc_ev=encoded_envelope"

        try {
            interactor.dispatchEnvelope(url)
        } catch (e: IllegalStateException) {
            assert(e.message == "LinkMode: Missing topic parameter")
        }
    }
}