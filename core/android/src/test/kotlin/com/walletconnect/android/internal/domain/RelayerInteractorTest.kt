package com.walletconnect.android.internal.domain

import com.walletconnect.android.internal.common.ConditionalExponentialBackoffStrategy
import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.crypto.codec.Codec
import com.walletconnect.android.internal.common.exception.WalletConnectException
import com.walletconnect.android.internal.common.json_rpc.data.JsonRpcSerializer
import com.walletconnect.android.internal.common.json_rpc.domain.relay.RelayJsonRpcInteractor
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.WCRequest
import com.walletconnect.android.internal.common.model.type.ClientParams
import com.walletconnect.android.internal.common.model.type.Error
import com.walletconnect.android.internal.common.model.type.JsonRpcClientSync
import com.walletconnect.android.internal.common.storage.push_messages.PushMessagesRepository
import com.walletconnect.android.internal.common.storage.rpc.JsonRpcHistory
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.android.relay.RelayConnectionInterface
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.foundation.network.model.Relay
import com.walletconnect.foundation.network.model.RelayDTO
import com.walletconnect.foundation.util.Logger
import com.walletconnect.utils.Empty
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import org.junit.BeforeClass
import org.junit.Test
import java.io.PrintWriter

@ExperimentalCoroutinesApi
internal class RelayerInteractorTest {

    private val relay: RelayConnectionInterface = mockk {
        every { subscriptionRequest } returns flow { }
    }

    private val backoffStrategy: ConditionalExponentialBackoffStrategy = mockk()

    private val jsonRpcHistory: JsonRpcHistory = mockk {
        every { setRequest(any(), any(), any(), any(), any()) } returns true
        every { updateRequestWithResponse(any(), any()) } returns mockk()
    }

    private val codec: Codec = mockk {
        every { encrypt(any(), any(), any()) } returns ByteArray(1)
    }

    private val pushMessagesRepository: PushMessagesRepository = mockk()

    private val logger: Logger = object : Logger {
        override fun log(logMsg: String?) {
            println(logMsg)
        }

        override fun log(throwable: Throwable?) {
            println(throwable)
        }

        override fun error(errorMsg: String?) {
            println(errorMsg)
        }

        override fun error(throwable: Throwable?) {
            println(throwable)
        }
    }

    private val sut =
        spyk(RelayJsonRpcInteractor(relay, codec, jsonRpcHistory, pushMessagesRepository, logger, backoffStrategy), recordPrivateCalls = true) {
            every { checkNetworkConnectivity() } answers { }
            every { relay.onResubscribe } returns flow { }
        }

    private val topicVO = Topic("mockkTopic")

    private val settlementSequence: JsonRpcClientSync<*> = mockk {
        every { id } returns DEFAULT_ID
        every { method } returns String.Empty
    }

    private val request: WCRequest = mockk {
        every { id } returns DEFAULT_ID
        every { topic } returns topicVO
    }

    private val peerError: Error = mockk {
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
        every { relay.publish(any(), any(), any(), any(), any()) } answers {
            lastArg<(Result<Relay.Model.Call.Publish.Acknowledgement>) -> Unit>().invoke(
                Result.success(mockk())
            )
        }
    }

    private fun mockRelayPublishFailure() {
        every { relay.publish(any(), any(), any(), any()) } answers {
            lastArg<(Result<Relay.Model.Call.Publish.JsonRpcError>) -> Unit>().invoke(
                Result.failure(mockk<Throwable>() {
                    every { printStackTrace(any<PrintWriter>()) } returns Unit
                })
            )
        }
    }

    private fun publishJsonRpcRequests() {
        val irnParamsVO = IrnParams(Tags.SESSION_PING, Ttl(300))
        sut.publishJsonRpcRequest(
            topicVO,
            irnParamsVO,
            settlementSequence,
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }

    companion object {
        private const val DEFAULT_ID = -1L

        @JvmStatic
        @BeforeClass
        fun beforeAll() {
            mockkObject(wcKoinApp)

            every { wcKoinApp.koin.get<JsonRpcSerializer>() } returns mockk()
            every { wcKoinApp.koin.get<JsonRpcSerializer>().serialize(any()) } returns ""
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
        every { jsonRpcHistory.setRequest(any(), any(), any(), any(), any()) } returns false
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
        val result = JsonRpcResponse.JsonRpcResult(request.id, result = params)
        val irnParams = IrnParams(Tags.SESSION_PING, Ttl(300))
        mockRelayPublishSuccess()
        sut.respondWithParams(request, params, irnParams, onSuccess = {}, onFailure = {})
        verify { sut.publishJsonRpcResponse(topic = topicVO, response = result, params = irnParams, onSuccess = any(), onFailure = any()) }
    }

    @Test
    fun `RespondWithSuccess publishes result as true with request id on request topic`() {
        val result = JsonRpcResponse.JsonRpcResult(request.id, result = true)
        val irnParams = IrnParams(Tags.SESSION_PING, Ttl(300))
        mockRelayPublishSuccess()
        sut.respondWithSuccess(request, irnParams)
        verify { sut.publishJsonRpcResponse(topic = topicVO, response = result, params = irnParams, onSuccess = any(), onFailure = any()) }
    }

    @Test
    fun `RespondWithError publishes result as error with request id on request topic`() {
        val error = JsonRpcResponse.Error(peerError.code, peerError.message)
        val result = JsonRpcResponse.JsonRpcError(request.id, error = error)
        val irnParams = IrnParams(Tags.SESSION_PING, Ttl(300))
        mockRelayPublishSuccess()
        sut.respondWithError(request, peerError, irnParams)
        verify { sut.publishJsonRpcResponse(topic = topicVO, response = result, params = irnParams, onSuccess = any(), onFailure = any()) }
    }

    @Test
    fun `OnFailure callback called when respondWithError encounters error`() {
        mockRelayPublishFailure()
        val irnParams = IrnParams(Tags.SESSION_PING, Ttl(300))
        sut.respondWithError(request = request, error = peerError, irnParams = irnParams, onFailure = onFailure)
        verify { onFailure(any()) }
    }

    @Test
    fun `OnFailure callback called when subscribe encounters error`() {
        every { relay.subscribe(any(), any(), any()) } answers {
            lastArg<(Result<RelayDTO.Publish.Result.Acknowledgement>) -> Unit>().invoke(
                Result.failure(Throwable("error"))
            )
        }
        sut.subscribe(topicVO, onFailure = onFailure)
        verify { onFailure(any()) }
    }
}