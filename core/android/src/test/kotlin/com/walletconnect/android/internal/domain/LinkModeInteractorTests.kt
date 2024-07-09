package com.walletconnect.android.internal.domain

import android.content.Context
import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.crypto.codec.Codec
import com.walletconnect.android.internal.common.json_rpc.data.JsonRpcSerializer
import com.walletconnect.android.internal.common.json_rpc.domain.link_mode.LinkModeJsonRpcInteractor
import com.walletconnect.android.internal.common.model.EnvelopeType
import com.walletconnect.android.internal.common.model.Participants
import com.walletconnect.android.internal.common.model.TransportType
import com.walletconnect.android.internal.common.model.sync.ClientJsonRpc
import com.walletconnect.android.internal.common.model.type.JsonRpcClientSync
import com.walletconnect.android.internal.common.storage.rpc.JsonRpcHistory
import com.walletconnect.foundation.common.model.Topic
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.KoinApplication
import kotlin.test.assertFailsWith

class LinkModeInteractorTests {
    private val chaChaPolyCodec: Codec = mockk()
    private val jsonRpcHistory: JsonRpcHistory = mockk()
    private val context: Context = mockk()
    private val wcKoinApp: KoinApplication = mockk()
    private val serializer: JsonRpcSerializer = mockk()
    private val interactor: LinkModeJsonRpcInteractor

    init {
        every { wcKoinApp.koin.get<JsonRpcSerializer>() } returns serializer
        interactor = LinkModeJsonRpcInteractor(chaChaPolyCodec, jsonRpcHistory, context)
    }

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test triggerRequest with valid data`() = testScope.runTest {
        val payload: JsonRpcClientSync<*> = mockk()
        val topic = Topic("test_topic")
        val appLink = "test_app_link"
        val envelopeType = EnvelopeType.ZERO
        val requestJson = "request_json"
        val encryptedResponse = "encrypted_response".toByteArray()

        every { serializer.serialize(payload) } returns requestJson
        every { jsonRpcHistory.setRequest(any(), any(), any(), any(), any()) } returns true
        every { chaChaPolyCodec.encrypt(any(), any(), any()) } returns encryptedResponse
        every { context.startActivity(any()) } just Runs

        interactor.triggerRequest(payload, topic, appLink, envelopeType)

        verify {
            serializer.serialize(payload)
            jsonRpcHistory.setRequest(payload.id, topic, payload.method, requestJson, TransportType.LINK_MODE)
            chaChaPolyCodec.encrypt(topic, requestJson, envelopeType)
            context.startActivity(any())
        }
    }

    @Test
    fun `test triggerRequest with serialization failure`() = testScope.runTest {
        val payload: JsonRpcClientSync<*> = mockk()
        val topic = Topic("test_topic")
        val appLink = "test_app_link"
        val envelopeType = EnvelopeType.ZERO

        every { serializer.serialize(payload) } returns null

        assertFailsWith<IllegalStateException>("LinkMode: Cannot serialize the request") {
            interactor.triggerRequest(payload, topic, appLink, envelopeType)
        }
    }

//    @Test
//    fun `test triggerResponse with valid data`() = testScope.runTest {
//        val topic = Topic("test_topic")
//        val response: JsonRpcResponse = mockk()
//        val appLink = "test_app_link"
//        val participants: Participants? = null
//        val envelopeType = EnvelopeType.ZERO
//        val responseJson = "response_json"
//        val encryptedResponse = "encrypted_response".toByteArray()
//        val jsonRpcRecord: JsonRpcRecord = mockk()
//
//        every { serializer.serialize(response) } returns responseJson
//        every { chaChaPolyCodec.encrypt(any(), any(), any(), any()) } returns encryptedResponse
//        every { context.startActivity(any()) } just Runs
//        every { jsonRpcHistory.updateRequestWithResponse(any(), any()) } just Runs
//
//        interactor.triggerResponse(topic, response, appLink, participants, envelopeType)
//
//        verify {
//            serializer.serialize(response)
//            chaChaPolyCodec.encrypt(topic, responseJson, envelopeType, participants)
//            context.startActivity(any())
//            jsonRpcHistory.updateRequestWithResponse(response.id, responseJson)
//        }
//    }

    @Test
    fun `test triggerResponse with serialization failure`() = testScope.runTest {
        val topic = Topic("test_topic")
        val response: JsonRpcResponse = mockk()
        val appLink = "test_app_link"
        val participants: Participants? = null
        val envelopeType = EnvelopeType.ZERO

        every { serializer.serialize(response) } returns null

        assertFailsWith<IllegalStateException>("LinkMode: Cannot serialize the response") {
            interactor.triggerResponse(topic, response, appLink, participants, envelopeType)
        }
    }

    @Test
    fun `test dispatchEnvelope with valid data`() = testScope.runTest {
        val url = "test_url?wc_ev=encoded_envelope&topic=test_topic"
        val encodedEnvelope = "encoded_envelope"
        val envelope = "decrypted_envelope"
        val clientJsonRpc: ClientJsonRpc = mockk()
        val jsonRpcResponse: JsonRpcResponse = mockk()

        every { chaChaPolyCodec.decrypt(any(), any()) } returns envelope
        coEvery { serializer.tryDeserialize<ClientJsonRpc>(any()) } returns clientJsonRpc
        coEvery { serializer.tryDeserialize<JsonRpcResponse>(any()) } returns null
        coEvery { serializer.tryDeserialize<JsonRpcResponse.JsonRpcError>(any()) } returns null

        interactor.dispatchEnvelope(url)

        coVerify {
            chaChaPolyCodec.decrypt(Topic("test_topic"), any())
            serializer.tryDeserialize<ClientJsonRpc>(envelope)
        }
    }

    @Test
    fun `test dispatchEnvelope with missing wc_ev parameter`() = testScope.runTest {
        val url = "test_url?topic=test_topic"

        assertFailsWith<IllegalStateException>("LinkMode: Missing wc_ev parameter") {
            interactor.dispatchEnvelope(url)
        }
    }

    @Test
    fun `test dispatchEnvelope with missing topic parameter`() = testScope.runTest {
        val url = "test_url?wc_ev=encoded_envelope"

        assertFailsWith<IllegalStateException>("LinkMode: Missing topic parameter") {
            interactor.dispatchEnvelope(url)
        }
    }

//    @Test
//    fun `test serializeRequest with valid data`() = testScope.runTest {
//        val clientJsonRpc: ClientJsonRpc = mockk()
//        val topic = "test_topic"
//        val envelope = "envelope"
//
//        every { jsonRpcHistory.setRequest(any(), any(), any(), any(), any()) } returns true
//        coEvery { serializer.deserialize(any(), any()) } returns mockk()
//
//        interactor.serializeRequest(clientJsonRpc, topic, envelope)
//
//        coVerify {
//            serializer.deserialize(clientJsonRpc.method, envelope)
//            _clientSyncJsonRpc.emit(any())
//        }
//    }
//
//    @Test
//    fun `test serializeResult with valid data`() = testScope.runTest {
//        val result: JsonRpcResponse.JsonRpcResult = mockk()
//        val serializedResult = "serialized_result"
//        val jsonRpcRecord: JsonRpcRecord = mockk()
//
//        every { serializer.serialize(result) } returns serializedResult
//        every { jsonRpcHistory.updateRequestWithResponse(any(), any()) } returns jsonRpcRecord
//        coEvery { serializer.deserialize(any(), any()) } returns mockk()
//
//        interactor.serializeResult(result)
//
//        coVerify {
//            serializer.serialize(result)
//            jsonRpcHistory.updateRequestWithResponse(result.id, serializedResult)
//            serializer.deserialize(jsonRpcRecord.method, jsonRpcRecord.body)
//            _peerResponse.emit(any())
//        }
//    }
//
//    @Test
//    fun `test serializeError with valid data`() = testScope.runTest {
//        val error: JsonRpcResponse.JsonRpcError = mockk()
//        val serializedResult = "serialized_result"
//        val jsonRpcRecord: JsonRpcRecord = mockk()
//
//        every { serializer.serialize(error) } returns serializedResult
//        every { jsonRpcHistory.updateRequestWithResponse(any(), any()) } returns jsonRpcRecord
//        coEvery { serializer.deserialize(any(), any()) } returns mockk()
//
//        interactor.serializeError(error)
//
//        coVerify {
//            serializer.serialize(error)
//            jsonRpcHistory.updateRequestWithResponse(error.id, serializedResult)
//            serializer.deserialize(jsonRpcRecord.method, jsonRpcRecord.body)
//            _peerResponse.emit(any())
//        }
//    }
}