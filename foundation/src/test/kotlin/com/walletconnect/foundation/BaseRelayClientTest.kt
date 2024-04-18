package com.walletconnect.foundation

import com.walletconnect.foundation.common.model.SubscriptionId
import com.walletconnect.foundation.network.BaseRelayClient
import com.walletconnect.foundation.network.data.service.RelayService
import com.walletconnect.foundation.network.model.Relay
import com.walletconnect.foundation.network.model.RelayDTO
import com.walletconnect.foundation.util.Logger
import com.walletconnect.foundation.util.scope
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi

class BaseRelayClientTest {
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private lateinit var client: BaseRelayClient
    private val relayServiceMock = mockk<RelayService>(relaxed = true)
    private val loggerMock = mockk<Logger>(relaxed = true)

    @Before
    fun setup() {
        client = object : BaseRelayClient() {
            init {
                this.relayService = relayServiceMock
                this.logger = loggerMock
                scope = testScope
            }
        }
    }

    @Test
    fun `test publish success`() = testScope.runTest {
        val topic = "testTopic"
        val message = "testMessage"
        val params = Relay.Model.IrnParams(1, 60, true)
        val ack = RelayDTO.Publish.Result.Acknowledgement(123L, result = true)

        coEvery { relayServiceMock.publishRequest(any()) } returns Unit
        coEvery { relayServiceMock.observePublishAcknowledgement() } returns flowOf(ack)

        client.observeResults()
        client.publish(topic, message, params, 123L) { result ->
            assertTrue(result.isSuccess)
            result.onSuccess {
                assertEquals(123L, it.id)
            }
        }

        coVerify { relayServiceMock.publishRequest(any()) }
    }

    @Test
    fun `test publish error due to time out`() = testScope.runTest {
        val topic = "testTopic"
        val message = "testMessage"
        val params = Relay.Model.IrnParams(1, 60, true)

        coEvery { relayServiceMock.publishRequest(any()) } returns Unit
        coEvery { relayServiceMock.observePublishAcknowledgement() } returns flow { delay(10000L) }

        client.publish(topic, message, params) { result ->
            assertTrue(result.isFailure)
        }

        testScheduler.apply { advanceTimeBy(5000); runCurrent() }

        coVerify { relayServiceMock.publishRequest(any()) }
    }

    @Test
    fun `test subscribe success`() = testScope.runTest {
        val topic = "testTopic"
        val expectedId = 123L
        val relayDto = RelayDTO.Subscribe.Result.Acknowledgement(id = expectedId, result = SubscriptionId("testId"))

        coEvery { relayServiceMock.subscribeRequest(any()) } returns Unit
        coEvery { relayServiceMock.observeSubscribeAcknowledgement() } returns flowOf(relayDto)

        client.observeResults()
        client.subscribe(topic, expectedId) { result ->
            assertTrue(result.isSuccess)
            result.onSuccess {
                assertEquals(expectedId, result.getOrNull()?.id)
            }
        }

        coVerify { relayServiceMock.subscribeRequest(any()) }
    }

    @Test
    fun `test subscribe failure due to timeout`() = testScope.runTest {
        val topic = "testTopic"

        coEvery { relayServiceMock.subscribeRequest(any()) } returns Unit
        coEvery { relayServiceMock.observeSubscribeAcknowledgement() } returns flow { delay(10000L) }

        client.subscribe(topic) { result ->
            assertTrue(result.isFailure)
            assertEquals("Subscribe timed out: Timed out waiting for 60000 ms", result.exceptionOrNull()?.message)
        }

        testScheduler.apply { advanceTimeBy(5000); runCurrent() }

        coVerify { relayServiceMock.subscribeRequest(any()) }
    }

    @Test
    fun `test batch subscribe success`() = testScope.runTest {
        val topics = listOf("testTopic")
        val expectedId = 123L
        val relayDto = RelayDTO.BatchSubscribe.Result.Acknowledgement(id = expectedId, result = listOf("testId"))

        coEvery { relayServiceMock.batchSubscribeRequest(any()) } returns Unit
        coEvery { relayServiceMock.observeBatchSubscribeAcknowledgement() } returns flowOf(relayDto)

        client.observeResults()
        client.batchSubscribe(topics, expectedId) { result ->
            assertTrue(result.isSuccess)
            result.onSuccess {
                assertEquals(expectedId, result.getOrNull()?.id)
            }
        }

        coVerify { relayServiceMock.batchSubscribeRequest(any()) }
    }

    @Test
    fun `test batch subscribe failure due to timeout`() = testScope.runTest {
        val topics = listOf("testTopic")

        coEvery { relayServiceMock.batchSubscribeRequest(any()) } returns Unit
        coEvery { relayServiceMock.observeBatchSubscribeAcknowledgement() } returns flow { delay(10000L) }

        client.batchSubscribe(topics) { result ->
            assertTrue(result.isFailure)
            assertEquals("Batch Subscribe timed out: Timed out waiting for 60000 ms", result.exceptionOrNull()?.message)
        }

        testScheduler.apply { advanceTimeBy(5000); runCurrent() }

        coVerify { relayServiceMock.batchSubscribeRequest(any()) }
    }
}