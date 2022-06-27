package com.walletconnect.sign.relay.domain

import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.core.exceptions.client.WalletConnectException
import com.walletconnect.sign.core.exceptions.peer.PeerError
import com.walletconnect.sign.core.model.type.ClientParams
import com.walletconnect.sign.core.model.type.SettlementSequence
import com.walletconnect.sign.core.model.vo.TopicVO
import com.walletconnect.sign.core.model.vo.jsonRpc.JsonRpcResponseVO
import com.walletconnect.sign.core.model.vo.sync.WCRequestVO
import com.walletconnect.sign.crypto.data.codec.ChaChaPolyCodec
import com.walletconnect.sign.relay.Relay
import com.walletconnect.sign.relay.data.serializer.JsonRpcSerializer
import com.walletconnect.sign.relay.model.RelayDTO
import com.walletconnect.sign.storage.history.JsonRpcHistory
import com.walletconnect.sign.util.Empty
import com.walletconnect.sign.util.Logger
import com.walletconnect.sign.util.NetworkState
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse

@ExperimentalCoroutinesApi
internal class RelayerInteractorTest {

    private val chaChaPolyCodec: ChaChaPolyCodec = mockk {
        every { encrypt(any(), any(), any(), any()) } returns String.Empty
    }

    private val relay: Relay = mockk {
        every { subscriptionRequest } returns flow { }
    }

    private val serializer: JsonRpcSerializer = mockk {
        every { serialize(any()) } returns String.Empty
    }

    private val jsonRpcHistory: JsonRpcHistory = mockk {
        every { setRequest(any(), any(), any(), any()) } returns true
        every { updateRequestWithResponse(any(), any()) } returns mockk()
    }

    private val networkState: NetworkState = mockk(relaxed = true) {
        every { isAvailable } returns MutableStateFlow(true).asStateFlow()
    }

    private val sut =
        spyk(
            RelayerInteractor(relay, serializer, chaChaPolyCodec, jsonRpcHistory, networkState),
            recordPrivateCalls = true
        ) {
            every { checkConnectionWorking() } answers { }
        }

    private val topicVO = TopicVO("mockkTopic")

    private val settlementSequence: SettlementSequence<*> = mockk {
        every { id } returns DEFAULT_ID
        every { method } returns String.Empty
    }

    private val request: WCRequestVO = mockk {
        every { id } returns DEFAULT_ID
        every { topic } returns topicVO
    }

    val peerError: PeerError = mockk {
        every { message } returns "message"
        every { code } returns -1
    }

    private val onFailure: (Throwable) -> Unit = mockk {
        every { this@mockk.invoke(any()) } returns Unit
    }

    private val onSuccess: () -> Unit = mockk {
        every { this@mockk.invoke() } returns Unit
    }

    private val onError: (WalletConnectException) -> Unit = mockk {
        every { this@mockk.invoke(any()) } returns Unit
    }

    private fun mockRelayPublishSuccess() {
        every { relay.publish(any(), any(), any(), any()) } answers {
            lastArg<(Result<Sign.Model.Relay.Call.Publish.Acknowledgement>) -> Unit>().invoke(
                Result.success(mockk())
            )
        }
    }

    private fun mockRelayPublishFailure() {
        every { relay.publish(any(), any(), any(), any()) } answers {
            lastArg<(Result<Sign.Model.Relay.Call.Publish.Acknowledgement>) -> Unit>().invoke(
                Result.failure(mockk())
            )
        }
    }

    private fun publishJsonRpcRequests() {
        sut.publishJsonRpcRequests(
            topicVO,
            settlementSequence,
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }

    companion object {
        private const val DEFAULT_ID = -1L

        @BeforeAll
        @JvmStatic
        fun beforeAll() {
            mockkObject(Logger)
            every { Logger.error(any<String>()) } answers {}
            every { Logger.log(any<String>()) } answers {}
        }

        @AfterAll
        @JvmStatic
        fun afterAll() {
            unmockkObject(Logger)
        }
    }

    @Test
    fun `OnSuccess callback called when publishJsonRpcRequests gets acknowledged`() {
        mockRelayPublishSuccess()
        publishJsonRpcRequests()
        verify { onSuccess() }
        verify { onFailure wasNot Called }
    }

    @Test
    fun `OnFailure callback called when publishJsonRpcRequests encounters error`() {
        mockRelayPublishFailure()
        publishJsonRpcRequests()
        verify { onFailure(any()) }
        verify { onSuccess wasNot Called }
    }

    @Test
    fun `PublishJsonRpcRequests called when setRequest returned false does not call any callback`() {
        every { jsonRpcHistory.setRequest(any(), any(), any(), any()) } returns false
        publishJsonRpcRequests()
        verify { onFailure wasNot Called }
        verify { onSuccess wasNot Called }
    }

    @Test
    fun `OnSuccess callback called when publishJsonRpcResponse gets acknowledged`() {
        mockRelayPublishSuccess()
        publishJsonRpcRequests()
        verify { onSuccess() }
        verify { onFailure wasNot Called }
    }

    @Test
    fun `OnFailure callback called when publishJsonRpcResponse encounters error`() {
        mockRelayPublishFailure()
        publishJsonRpcRequests()
        verify { onFailure(any()) }
        verify { onSuccess wasNot Called }
    }

    @Test
    fun `RespondWithParams publishes result with params and request id on request topic`() {
        val params: ClientParams = mockk()
        val result = JsonRpcResponseVO.JsonRpcResult(request.id, result = params)
        mockRelayPublishSuccess()
        sut.respondWithParams(request, params)
        verify { sut.publishJsonRpcResponse(topicVO, result, any(), any()) }
    }

    @Test
    fun `RespondWithSuccess publishes result as true with request id on request topic`() {
        val result = JsonRpcResponseVO.JsonRpcResult(request.id, result = true)
        mockRelayPublishSuccess()
        sut.respondWithSuccess(request)
        verify { sut.publishJsonRpcResponse(topicVO, result, any(), any()) }
    }

    @Test
    fun `RespondWithError publishes result as error with request id on request topic`() {
        val error = JsonRpcResponseVO.Error(peerError.code, peerError.message)
        val result = JsonRpcResponseVO.JsonRpcError(request.id, error = error)
        mockRelayPublishSuccess()
        sut.respondWithError(request, peerError)
        verify { sut.publishJsonRpcResponse(topicVO, result, any(), any()) }
    }

    @Test
    fun `OnFailure callback called when respondWithError encounters error`() {
        mockRelayPublishFailure()
        sut.respondWithError(request, peerError, onFailure)
        verify { onFailure(any()) }
    }

    @Test
    fun `OnFailure callback called when subscribe encounters error`() {
        every { relay.subscribe(any(), any()) } answers {
            lastArg<(Result<RelayDTO.Publish.Acknowledgement>) -> Unit>().invoke(
                Result.failure(mockk())
            )
        }
        sut.subscribe(topicVO)
        verify { Logger.error(any<String>()) }
    }

    @Test
    fun `InitializationErrorsFlow emits value only on OnConnectionFailed`() = runBlockingTest {
        every { relay.eventsFlow } returns flowOf(
            mockk<Sign.Model.Relay.Event.OnConnectionOpened<*>>(),
            mockk<Sign.Model.Relay.Event.OnMessageReceived>(),
            mockk<Sign.Model.Relay.Event.OnConnectionClosing>(),
            mockk<Sign.Model.Relay.Event.OnConnectionClosed>(),
            mockk<Sign.Model.Relay.Event.OnConnectionFailed>() {
                every { throwable } returns RuntimeException()
            }
        ).shareIn(this, SharingStarted.Lazily)

        val job = sut.initializationErrorsFlow.onEach { walletConnectException ->
            onError(walletConnectException)
        }.launchIn(this)

        verify(exactly = 1) { onError(any()) }

        job.cancelAndJoin()
    }

    @Test
    fun `IsConnectionOpened initial value is false`() = runBlockingTest {
        assertFalse(sut.isConnectionAvailable.first())
    }
}